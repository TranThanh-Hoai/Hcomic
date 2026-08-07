package com.comic.h.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.comic.h.dto.response.ChapterImageResponse;
import com.comic.h.service.ChapterImageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChapterImageController {

    private final ChapterImageService chapterImageService;

    @PreAuthorize("hasAnyRole('TRANSLATOR', 'ADMIN')")
    @PostMapping(value = "/api/chapters/{chapterId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChapterImageResponse> uploadOrReplaceImage(
            @PathVariable Long chapterId,
            @RequestParam("image") MultipartFile image,
            @RequestParam("pageNumber") Integer pageNumber) {
        ChapterImageResponse response = chapterImageService.uploadOrReplaceImage(chapterId, image, pageNumber);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('TRANSLATOR', 'ADMIN')")
    @PostMapping(value = "/api/chapters/{chapterId}/images/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ChapterImageResponse>> uploadImagesBatch(
            @PathVariable Long chapterId,
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam(name = "startPageNumber", defaultValue = "1") Integer startPageNumber) {
        List<ChapterImageResponse> response = chapterImageService.uploadImagesBatch(chapterId, images, startPageNumber);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/chapters/{chapterId}/images")
    public ResponseEntity<List<ChapterImageResponse>> getChapterImages(@PathVariable Long chapterId) {
        return ResponseEntity.ok(chapterImageService.getChapterImages(chapterId));
    }

    @GetMapping("/api/chapters/{chapterId}/images/count")
    public ResponseEntity<Integer> countImages(@PathVariable Long chapterId) {
        return ResponseEntity.ok(chapterImageService.countImages(chapterId));
    }

    @PreAuthorize("hasAnyRole('TRANSLATOR', 'ADMIN')")
    @DeleteMapping("/api/chapters/{chapterId}/images/{pageNumber}")
    public ResponseEntity<String> deleteImageByPageNumber(
            @PathVariable Long chapterId,
            @PathVariable Integer pageNumber) {
        chapterImageService.deleteImageByPageNumber(chapterId, pageNumber);
        return ResponseEntity.ok("Image at page number " + pageNumber + " deleted successfully");
    }

    @PreAuthorize("hasAnyRole('TRANSLATOR', 'ADMIN')")
    @DeleteMapping("/api/chapters/{chapterId}/images")
    public ResponseEntity<String> deleteAllImages(@PathVariable Long chapterId) {
        long deletedCount = chapterImageService.deleteAllImages(chapterId);
        return ResponseEntity.ok("Deleted " + deletedCount + " images for chapter with id: " + chapterId);
    }
}
