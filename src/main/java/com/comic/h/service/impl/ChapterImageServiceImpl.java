package com.comic.h.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.comic.h.dto.response.ChapterImageResponse;
import com.comic.h.entity.Chapter;
import com.comic.h.entity.ChapterImage;
import com.comic.h.exception.BadRequestException;
import com.comic.h.exception.ResourceNotFoundException;
import com.comic.h.repository.ChapterImageRepository;
import com.comic.h.repository.ChapterRepository;
import com.comic.h.security.ComicSecurityEvaluator;
import com.comic.h.service.ChapterImageService;
import com.comic.h.service.FileStorageService;
import com.comic.h.util.ImageProcessor;

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
    private final FileStorageService fileStorageService;
    private final ImageProcessor imageProcessor;
    private final ComicSecurityEvaluator comicSecurityEvaluator;

    @Override
    @Transactional
    public ChapterImageResponse uploadOrReplaceImage(Long chapterId, MultipartFile image, Integer pageNumber) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + chapterId));

        comicSecurityEvaluator.verifyOwnership(chapter.getComic());

        return processSaveOrReplace(chapter, image, pageNumber);
    }

    @Override
    public List<ChapterImageResponse> uploadImagesBatch(Long chapterId, List<MultipartFile> images, Integer startPageNumber) {
        if (images == null || images.isEmpty()) {
            throw new BadRequestException("Image batch cannot be empty");
        }

        if (images.size() > 50) {
            throw new BadRequestException("Image batch cannot exceed 50 files per request");
        }

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + chapterId));

        comicSecurityEvaluator.verifyOwnership(chapter.getComic());

        chapter.setUploadStatus("UPLOADING");
        chapterRepository.save(chapter);

        int startPage = (startPageNumber != null && startPageNumber > 0) ? startPageNumber : 1;
        List<ChapterImageResponse> responses = new ArrayList<>();
        List<String> newlySavedPaths = new ArrayList<>();

        try {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);
                int pageNum = startPage + i;
                ChapterImageResponse resp = null;
                int fileRetries = 0;
                while (true) {
                    try {
                        resp = processSaveOrReplace(chapter, file, pageNum);
                        break;
                    } catch (BadRequestException ex) {
                        fileRetries++;
                        if (fileRetries > 3 || !ex.getMessage().contains("Failed to save image")) {
                            throw ex;
                        }
                        log.warn("Retrying image upload for page {} (attempt {}) after error: {}", pageNum, fileRetries, ex.getMessage());
                    }
                }
                responses.add(resp);
                if (resp != null && resp.getImageUrl() != null) {
                    newlySavedPaths.add(resp.getImageUrl());
                }
            }

            chapter.setUploadStatus("COMPLETED");
            chapterRepository.save(chapter);
            return responses;
        } catch (Exception ex) {
            chapter.setUploadStatus("FAILED");
            chapterRepository.save(chapter);
            if (!newlySavedPaths.isEmpty()) {
                fileStorageService.deleteFiles(newlySavedPaths);
            }
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int countImages(Long chapterId) {
        List<ChapterImage> images = chapterImageRepository.findByChapterIdOrderByPageNumberAsc(chapterId);
        if (images.isEmpty() && !chapterRepository.existsById(chapterId)) {
            throw new ResourceNotFoundException("Chapter not found with id: " + chapterId);
        }
        return images.size();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChapterImageResponse> getChapterImages(Long chapterId) {
        List<ChapterImage> images = chapterImageRepository.findByChapterIdOrderByPageNumberAsc(chapterId);
        if (images.isEmpty() && !chapterRepository.existsById(chapterId)) {
            throw new ResourceNotFoundException("Chapter not found with id: " + chapterId);
        }

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

        comicSecurityEvaluator.verifyOwnership(chapter.getComic());

        ChapterImage image = chapterImageRepository.findByChapterIdAndPageNumber(chapterId, pageNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Image at page number " + pageNumber + " not found for chapter " + chapterId));

        String oldFilePath = image.getImagePath();
        chapterImageRepository.deleteByChapterIdAndPageNumber(chapterId, pageNumber);

        fileStorageService.scheduleFileCleanupOnCommit(List.of(oldFilePath), null);
    }

    @Override
    @Transactional
    public long deleteAllImages(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + chapterId));

        comicSecurityEvaluator.verifyOwnership(chapter.getComic());

        List<ChapterImage> images = chapterImageRepository.findByChapterIdOrderByPageNumberAsc(chapterId);
        List<String> filePaths = images.stream()
                .map(ChapterImage::getImagePath)
                .toList();

        long deletedCount = chapterImageRepository.deleteByChapterId(chapterId);
        log.info("Deleted {} chapter image records for chapterId={}", deletedCount, chapterId);

        fileStorageService.scheduleFileCleanupOnCommit(filePaths, null);
        return deletedCount;
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
            byte[] webpBytes = imageProcessor.convertToWebp(image);
            savedPath = fileStorageService.saveFile(webpBytes, chapterDir.toString(), fileName).replace('\\', '/');
        } catch (Exception e) {
            throw new BadRequestException("Failed to save image for page " + pageNumber + ": " + e.getMessage());
        }

        fileStorageService.scheduleFileCleanupOnCommit(null, List.of(savedPath));

        try {
            var existingImageOpt = chapterImageRepository.findByChapterIdAndPageNumber(chapter.getId(), pageNumber);

            if (existingImageOpt.isPresent()) {
                ChapterImage existingImage = existingImageOpt.get();
                String oldPath = existingImage.getImagePath();
                existingImage.setImagePath(savedPath);
                chapterImageRepository.save(existingImage);

                fileStorageService.scheduleFileCleanupOnCommit(List.of(oldPath), null);
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
