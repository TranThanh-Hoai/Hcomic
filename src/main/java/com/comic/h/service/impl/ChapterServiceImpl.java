package com.comic.h.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.comic.h.dto.request.ChapterRequest;
import com.comic.h.dto.response.ChapterDetailResponse;
import com.comic.h.dto.response.ChapterImageResponse;
import com.comic.h.dto.response.ChapterResponse;
import com.comic.h.entity.Chapter;
import com.comic.h.entity.ChapterImage;
import com.comic.h.entity.Comic;
import com.comic.h.exception.BadRequestException;
import com.comic.h.exception.ForbiddenException;
import com.comic.h.exception.ResourceNotFoundException;
import com.comic.h.repository.ChapterRepository;
import com.comic.h.repository.ComicRepository;
import com.comic.h.service.ChapterService;
import com.comic.h.util.UploadUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChapterServiceImpl implements ChapterService {

    @Value("${app.upload.comic-dir:upload/comic}")
    private String comicUploadDir = "upload/comic";

    private final ChapterRepository chapterRepository;
    private final ComicRepository comicRepository;

    @Override
    @Transactional
    public ChapterResponse createChapter(Long comicId, ChapterRequest request, List<MultipartFile> images) {
        Comic comic = comicRepository.findById(comicId)
                .orElseThrow(() -> new ResourceNotFoundException("Comic not found with id: " + comicId));

        verifyComicOwnership(comic);

        if (request.getChapterNumber() == null) {
            throw new BadRequestException("Chapter number is required");
        }

        if (chapterRepository.existsByComicIdAndChapterNumber(comicId, request.getChapterNumber())) {
            throw new BadRequestException("Chapter number " + formatChapterNumber(request.getChapterNumber()) + " already exists for this comic");
        }

        if (images == null || images.isEmpty()) {
            throw new BadRequestException("Chapter images cannot be empty");
        }

        String chapterNumStr = formatChapterNumber(request.getChapterNumber());
        String slug = "chuong-" + chapterNumStr;

        String title = request.getTitle();
        if (title == null || title.trim().isEmpty()) {
            title = "Chương " + chapterNumStr;
        }

        String comicSlug = comic.getSlug();
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
        String chapterDirName = comicSlug + "-chapter-" + chapterNumStr + "-" + timestamp;
        Path chapterDir = Paths.get(comicUploadDir, comicSlug, chapterDirName);

        List<String> savedPaths;
        try {
            savedPaths = UploadUtils.saveFiles(images, chapterDir, comicSlug);
        } catch (Exception e) {
            throw new BadRequestException("Failed to save chapter images: " + e.getMessage());
        }

        scheduleDirectoryCleanupOnRollback(chapterDir, savedPaths);

        try {
            Chapter chapter = Chapter.builder()
                    .comic(comic)
                    .chapterNumber(request.getChapterNumber())
                    .title(title)
                    .slug(slug)
                    .viewCount(0L)
                    .images(new ArrayList<>())
                    .build();

            int pageNum = 1;
            for (String savedPath : savedPaths) {
                ChapterImage image = ChapterImage.builder()
                        .imagePath(savedPath)
                        .pageNumber(pageNum++)
                        .build();
                chapter.addImage(image);
            }

            Chapter savedChapter = chapterRepository.save(chapter);
            return mapToChapterResponse(savedChapter);
        } catch (RuntimeException e) {
            if (!TransactionSynchronizationManager.isActualTransactionActive()) {
                UploadUtils.deleteDirectory(chapterDir);
            }
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChapterResponse> getChaptersByComicSlug(String comicSlug, String sort) {
        if (!comicRepository.existsBySlug(comicSlug)) {
            throw new ResourceNotFoundException("Comic not found with slug: " + comicSlug);
        }

        List<Chapter> chapters;
        if ("asc".equalsIgnoreCase(sort)) {
            chapters = chapterRepository.findByComicSlugOrderByChapterNumberAsc(comicSlug);
        } else {
            chapters = chapterRepository.findByComicSlugOrderByChapterNumberDesc(comicSlug);
        }

        return chapters.stream()
                .map(this::mapToChapterResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChapterResponse> getChaptersByComicId(Long comicId, String sort) {
        if (!comicRepository.existsById(comicId)) {
            throw new ResourceNotFoundException("Comic not found with id: " + comicId);
        }

        List<Chapter> chapters;
        if ("asc".equalsIgnoreCase(sort)) {
            chapters = chapterRepository.findByComicIdOrderByChapterNumberAsc(comicId);
        } else {
            chapters = chapterRepository.findByComicIdOrderByChapterNumberDesc(comicId);
        }

        return chapters.stream()
                .map(this::mapToChapterResponse)
                .toList();
    }

    @Override
    @Transactional
    public ChapterDetailResponse getChapterDetailBySlug(String comicSlug, String chapterSlug) {
        Chapter chapter = chapterRepository.findByComicSlugAndSlug(comicSlug, chapterSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with slug: " + chapterSlug + " for comic: " + comicSlug));

        chapterRepository.incrementViewCount(chapter.getId());
        chapter.setViewCount(chapter.getViewCount() + 1);

        Comic comic = chapter.getComic();

        Optional<Chapter> prevChapterOpt = chapterRepository
                .findFirstByComicIdAndChapterNumberLessThanOrderByChapterNumberDesc(comic.getId(), chapter.getChapterNumber());
        Optional<Chapter> nextChapterOpt = chapterRepository
                .findFirstByComicIdAndChapterNumberGreaterThanOrderByChapterNumberAsc(comic.getId(), chapter.getChapterNumber());

        List<ChapterImageResponse> imageResponses = chapter.getImages().stream()
                .sorted(Comparator.comparingInt(ChapterImage::getPageNumber))
                .map(img -> ChapterImageResponse.builder()
                        .id(img.getId())
                        .pageNumber(img.getPageNumber())
                        .imagePath(img.getImagePath())
                        .build())
                .toList();

        return ChapterDetailResponse.builder()
                .id(chapter.getId())
                .comicId(comic.getId())
                .comicTitle(comic.getTitle())
                .comicSlug(comic.getSlug())
                .chapterNumber(chapter.getChapterNumber())
                .title(chapter.getTitle())
                .slug(chapter.getSlug())
                .viewCount(chapter.getViewCount())
                .createdAt(chapter.getCreatedAt())
                .updatedAt(chapter.getUpdatedAt())
                .images(imageResponses)
                .prevChapterSlug(prevChapterOpt.map(Chapter::getSlug).orElse(null))
                .nextChapterSlug(nextChapterOpt.map(Chapter::getSlug).orElse(null))
                .build();
    }

    @Override
    @Transactional
    public ChapterResponse updateChapter(Long chapterId, ChapterRequest request, List<MultipartFile> images) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + chapterId));

        verifyComicOwnership(chapter.getComic());

        if (request.getChapterNumber() != null && !request.getChapterNumber().equals(chapter.getChapterNumber())) {
            if (chapterRepository.existsByComicIdAndChapterNumberAndIdNot(chapter.getComic().getId(), request.getChapterNumber(), chapterId)) {
                throw new BadRequestException("Chapter number " + formatChapterNumber(request.getChapterNumber()) + " already exists for this comic");
            }
            chapter.setChapterNumber(request.getChapterNumber());
            chapter.setSlug("chuong-" + formatChapterNumber(request.getChapterNumber()));
        }

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            chapter.setTitle(request.getTitle());
        }

        List<String> newPaths = null;
        Path newChapterDir = null;
        Path oldChapterDir = null;

        if (images != null && !images.isEmpty()) {
            if (chapter.getImages() != null && !chapter.getImages().isEmpty()) {
                String firstOldPath = chapter.getImages().get(0).getImagePath();
                if (firstOldPath != null) {
                    oldChapterDir = Paths.get(firstOldPath).getParent();
                }
            }

            List<String> oldPaths = chapter.getImages().stream()
                    .map(ChapterImage::getImagePath)
                    .toList();

            String comicSlug = chapter.getComic().getSlug();
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
            String chapterDirName = comicSlug + "-chapter-" + formatChapterNumber(chapter.getChapterNumber()) + "-" + timestamp;
            newChapterDir = Paths.get(comicUploadDir, comicSlug, chapterDirName);

            try {
                newPaths = UploadUtils.saveFiles(images, newChapterDir, comicSlug);
            } catch (Exception e) {
                throw new BadRequestException("Failed to save new chapter images: " + e.getMessage());
            }

            scheduleFileCleanupOnCommit(oldPaths, newPaths);
            if (oldChapterDir != null) {
                scheduleDirectoryCleanupOnCommit(oldChapterDir, newChapterDir);
            }

            chapter.clearImages();

            int pageNum = 1;
            for (String newPath : newPaths) {
                ChapterImage image = ChapterImage.builder()
                        .imagePath(newPath)
                        .pageNumber(pageNum++)
                        .build();
                chapter.addImage(image);
            }
        }

        try {
            Chapter updatedChapter = chapterRepository.save(chapter);
            return mapToChapterResponse(updatedChapter);
        } catch (RuntimeException e) {
            if (!TransactionSynchronizationManager.isActualTransactionActive()) {
                if (newChapterDir != null) {
                    UploadUtils.deleteDirectory(newChapterDir);
                } else if (newPaths != null) {
                    UploadUtils.deleteFiles(newPaths);
                }
            }
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteChapter(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + chapterId));

        verifyComicOwnership(chapter.getComic());

        List<String> filePaths = chapter.getImages().stream()
                .map(ChapterImage::getImagePath)
                .toList();

        Path chapterDir = null;
        if (chapter.getImages() != null && !chapter.getImages().isEmpty()) {
            String firstPath = chapter.getImages().get(0).getImagePath();
            if (firstPath != null) {
                chapterDir = Paths.get(firstPath).getParent();
            }
        }

        scheduleFileCleanupOnCommit(filePaths, null);
        if (chapterDir != null) {
            scheduleDirectoryCleanupOnCommit(chapterDir, null);
        }

        chapterRepository.delete(chapter);
    }

    private void verifyComicOwnership(Comic comic) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("User is not authenticated");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return;
        }

        String currentUsername = authentication.getName();
        if (comic.getUploader() == null || comic.getUploader().getUsername() == null || !comic.getUploader().getUsername().equalsIgnoreCase(currentUsername)) {
            throw new ForbiddenException("You do not have permission to modify chapters for this comic");
        }
    }

    private void scheduleDirectoryCleanupOnRollback(Path dirToDeleteOnRollback, List<String> filesToDeleteOnRollback) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status != TransactionSynchronization.STATUS_COMMITTED) {
                        if (dirToDeleteOnRollback != null) {
                            UploadUtils.deleteDirectory(dirToDeleteOnRollback);
                        } else if (filesToDeleteOnRollback != null && !filesToDeleteOnRollback.isEmpty()) {
                            UploadUtils.deleteFiles(filesToDeleteOnRollback);
                        }
                    }
                }
            });
        }
    }

    private void scheduleDirectoryCleanupOnCommit(Path dirToDeleteOnCommit, Path dirToDeleteOnRollback) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == TransactionSynchronization.STATUS_COMMITTED) {
                        if (dirToDeleteOnCommit != null) {
                            UploadUtils.deleteDirectory(dirToDeleteOnCommit);
                        }
                    } else {
                        if (dirToDeleteOnRollback != null) {
                            UploadUtils.deleteDirectory(dirToDeleteOnRollback);
                        }
                    }
                }
            });
        } else {
            if (dirToDeleteOnCommit != null) {
                UploadUtils.deleteDirectory(dirToDeleteOnCommit);
            }
        }
    }

    private void scheduleFileCleanupOnCommit(List<String> filesToDeleteOnCommit, List<String> filesToDeleteOnRollback) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == TransactionSynchronization.STATUS_COMMITTED) {
                        if (filesToDeleteOnCommit != null && !filesToDeleteOnCommit.isEmpty()) {
                            UploadUtils.deleteFiles(filesToDeleteOnCommit);
                        }
                    } else {
                        if (filesToDeleteOnRollback != null && !filesToDeleteOnRollback.isEmpty()) {
                            UploadUtils.deleteFiles(filesToDeleteOnRollback);
                        }
                    }
                }
            });
        } else {
            if (filesToDeleteOnCommit != null && !filesToDeleteOnCommit.isEmpty()) {
                UploadUtils.deleteFiles(filesToDeleteOnCommit);
            }
        }
    }

    private ChapterResponse mapToChapterResponse(Chapter chapter) {
        return ChapterResponse.builder()
                .id(chapter.getId())
                .comicId(chapter.getComic().getId())
                .chapterNumber(chapter.getChapterNumber())
                .title(chapter.getTitle())
                .slug(chapter.getSlug())
                .viewCount(chapter.getViewCount())
                .imageCount(chapter.getImages() != null ? chapter.getImages().size() : 0)
                .createdAt(chapter.getCreatedAt())
                .updatedAt(chapter.getUpdatedAt())
                .build();
    }

    private String formatChapterNumber(Double chapterNumber) {
        if (chapterNumber == null) {
            return "0";
        }
        if (chapterNumber == chapterNumber.longValue()) {
            return String.valueOf(chapterNumber.longValue());
        }
        return String.valueOf(chapterNumber);
    }
}
