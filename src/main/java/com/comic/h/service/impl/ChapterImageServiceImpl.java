package com.comic.h.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.comic.h.dto.response.ChapterImageResponse;
import com.comic.h.entity.Chapter;
import com.comic.h.entity.ChapterImage;
import com.comic.h.entity.Comic;
import com.comic.h.exception.BadRequestException;
import com.comic.h.exception.ForbiddenException;
import com.comic.h.exception.ResourceNotFoundException;
import com.comic.h.repository.ChapterImageRepository;
import com.comic.h.repository.ChapterRepository;
import com.comic.h.service.ChapterImageService;
import com.comic.h.util.UploadUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChapterImageServiceImpl implements ChapterImageService {

    @Value("${app.upload.comic-dir:upload/comic}")
    private String comicUploadDir = "upload/comic";

    private final ChapterRepository chapterRepository;
    private final ChapterImageRepository chapterImageRepository;

    @Override
    @Transactional
    public ChapterImageResponse uploadOrReplaceImage(Long chapterId, MultipartFile image, Integer pageNumber) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + chapterId));

        verifyChapterOwnership(chapter);

        return processSaveOrReplace(chapter, image, pageNumber);
    }

    @Override
    @Transactional
    public List<ChapterImageResponse> uploadImagesBatch(Long chapterId, List<MultipartFile> images, Integer startPageNumber) {
        if (images == null || images.isEmpty()) {
            throw new BadRequestException("Image batch cannot be empty");
        }

        if (images.size() > 50) {
            throw new BadRequestException("Image batch cannot exceed 50 files per request");
        }

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + chapterId));

        verifyChapterOwnership(chapter);

        chapter.setUploadStatus("UPLOADING");
        chapterRepository.save(chapter);

        int startPage = (startPageNumber != null && startPageNumber > 0) ? startPageNumber : 1;
        List<ChapterImageResponse> responses = new ArrayList<>();
        AtomicInteger retryCount = new AtomicInteger(0);

        try {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);
                int pageNum = startPage + i;
                ChapterImageResponse resp = null;
                while (true) {
                    try {
                        resp = processSaveOrReplace(chapter, file, pageNum);
                        break;
                    } catch (BadRequestException ex) {
                        if (retryCount.incrementAndGet() > 3 || !ex.getMessage().contains("Failed to save image")) {
                            throw ex;
                        }
                        log.warn("Retrying image upload for page {} after error: {}", pageNum, ex.getMessage());
                    }
                }
                responses.add(resp);
            }

            chapter.setUploadStatus("COMPLETED");
            chapterRepository.save(chapter);
            return responses;
        } catch (Exception ex) {
            chapter.setUploadStatus("FAILED");
            chapterRepository.save(chapter);
            cleanupPartialImages(chapter);
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int countImages(Long chapterId) {
        if (!chapterRepository.existsById(chapterId)) {
            throw new ResourceNotFoundException("Chapter not found with id: " + chapterId);
        }
        return chapterImageRepository.findByChapterIdOrderByPageNumberAsc(chapterId).size();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChapterImageResponse> getChapterImages(Long chapterId) {
        if (!chapterRepository.existsById(chapterId)) {
            throw new ResourceNotFoundException("Chapter not found with id: " + chapterId);
        }

        List<ChapterImage> images = chapterImageRepository.findByChapterIdOrderByPageNumberAsc(chapterId);
        return images.stream()
                .map(img -> ChapterImageResponse.builder()
                        .pageNumber(img.getPageNumber())
                        .imageUrl(img.getImagePath())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void deleteImageByPageNumber(Long chapterId, Integer pageNumber) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + chapterId));

        verifyChapterOwnership(chapter);

        ChapterImage image = chapterImageRepository.findByChapterIdAndPageNumber(chapterId, pageNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Image at page number " + pageNumber + " not found for chapter " + chapterId));

        String oldFilePath = image.getImagePath();
        chapterImageRepository.deleteByChapterIdAndPageNumber(chapterId, pageNumber);

        scheduleFileCleanupOnCommit(List.of(oldFilePath), null);
    }

    @Override
    @Transactional
    public long deleteAllImages(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + chapterId));

        verifyChapterOwnership(chapter);

        List<ChapterImage> images = chapterImageRepository.findByChapterIdOrderByPageNumberAsc(chapterId);
        List<String> filePaths = images.stream()
                .map(ChapterImage::getImagePath)
                .toList();

        long deletedCount = chapterImageRepository.deleteByChapterId(chapterId);
        log.info("Deleted {} chapter image records for chapterId={}", deletedCount, chapterId);

        scheduleFileCleanupOnCommit(filePaths, null);
        return deletedCount;
    }

    private void cleanupPartialImages(Chapter chapter) {
        if (chapter.getImages() == null || chapter.getImages().isEmpty()) {
            return;
        }

        List<String> pathsToDelete = new ArrayList<>();
        for (ChapterImage image : chapter.getImages()) {
            if (image != null && image.getImagePath() != null) {
                pathsToDelete.add(image.getImagePath());
            }
        }

        chapter.clearImages();
        chapterRepository.save(chapter);
        if (!pathsToDelete.isEmpty()) {
            UploadUtils.deleteFiles(pathsToDelete);
        }
    }

    private ChapterImageResponse processSaveOrReplace(Chapter chapter, MultipartFile image, Integer pageNumber) {
        if (pageNumber == null || pageNumber < 1) {
            throw new BadRequestException("Page number must be a positive integer");
        }

        if (image == null || image.isEmpty()) {
            throw new BadRequestException("Image file cannot be empty for page number " + pageNumber);
        }

        String comicSlug = chapter.getComic().getSlug();
        String chapterNumStr = formatChapterNumber(chapter.getChapterNumber());
        String chapterDirName = comicSlug + "-chapter-" + chapterNumStr;
        Path chapterDir = Paths.get(comicUploadDir, comicSlug, chapterDirName);

        String fileName = String.format("page-%03d-%s.webp", pageNumber, UUID.randomUUID().toString().substring(0, 8));

        String savedPath;
        try {
            savedPath = UploadUtils.saveFile(image, chapterDir, fileName);
        } catch (Exception e) {
            throw new BadRequestException("Failed to save image for page " + pageNumber + ": " + e.getMessage());
        }

        scheduleFileCleanupOnRollback(List.of(savedPath));

        try {
            var existingImageOpt = chapterImageRepository.findByChapterIdAndPageNumber(chapter.getId(), pageNumber);

            if (existingImageOpt.isPresent()) {
                ChapterImage existingImage = existingImageOpt.get();
                String oldPath = existingImage.getImagePath();
                existingImage.setImagePath(savedPath);
                chapterImageRepository.save(existingImage);

                scheduleFileCleanupOnCommit(List.of(oldPath), null);
            } else {
                ChapterImage newImage = ChapterImage.builder()
                        .chapter(chapter)
                        .pageNumber(pageNumber)
                        .imagePath(savedPath)
                        .build();
                chapterImageRepository.save(newImage);
            }

            return ChapterImageResponse.builder()
                    .pageNumber(pageNumber)
                    .imageUrl(savedPath)
                    .build();
        } catch (DataIntegrityViolationException e) {
            log.warn("Data integrity violation when saving page {} for chapter {}: {}", pageNumber, chapter.getId(), e.getMessage());
            throw new BadRequestException("Page number " + pageNumber + " is currently being modified or already exists");
        }
    }

    private void verifyChapterOwnership(Chapter chapter) {
        Comic comic = chapter.getComic();
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

    private void scheduleFileCleanupOnRollback(List<String> filesToDeleteOnRollback) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status != TransactionSynchronization.STATUS_COMMITTED) {
                        if (filesToDeleteOnRollback != null && !filesToDeleteOnRollback.isEmpty()) {
                            UploadUtils.deleteFiles(filesToDeleteOnRollback);
                        }
                    }
                }
            });
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
