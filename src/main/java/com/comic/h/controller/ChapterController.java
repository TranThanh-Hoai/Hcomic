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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.comic.h.dto.request.ChapterRequest;
import com.comic.h.dto.response.ChapterDetailResponse;
import com.comic.h.dto.response.ChapterResponse;
import com.comic.h.service.ChapterService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;

    @PreAuthorize("hasAnyRole('TRANSLATOR', 'ADMIN')")
    @PostMapping(value = "/api/comics/{comicId}/chapters", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChapterResponse> createChapter(
            @PathVariable Long comicId,
            @Valid @RequestBody ChapterRequest request) {
        ChapterResponse response = chapterService.createChapter(comicId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/comics/slug/{comicSlug}/chapters")
    public ResponseEntity<List<ChapterResponse>> getChaptersByComicSlug(
            @PathVariable String comicSlug,
            @RequestParam(name = "sort", defaultValue = "desc") String sort) {
        return ResponseEntity.ok(chapterService.getChaptersByComicSlug(comicSlug, sort));
    }

    @GetMapping("/api/comics/slug/{comicSlug}/chapters/{chapterSlug}")
    public ResponseEntity<ChapterDetailResponse> getChapterDetailBySlug(
            @PathVariable String comicSlug,
            @PathVariable String chapterSlug) {
        return ResponseEntity.ok(chapterService.getChapterDetailBySlug(comicSlug, chapterSlug));
    }

    @GetMapping("/api/comics/{comicId}/chapters")
    public ResponseEntity<List<ChapterResponse>> getChaptersByComicId(
            @PathVariable Long comicId,
            @RequestParam(name = "sort", defaultValue = "desc") String sort) {
        return ResponseEntity.ok(chapterService.getChaptersByComicId(comicId, sort));
    }

    @GetMapping("/api/chapters/{chapterId}/status")
    public ResponseEntity<ChapterResponse> getChapterStatus(@PathVariable Long chapterId) {
        return ResponseEntity.ok(chapterService.getChapterById(chapterId));
    }

    @PreAuthorize("hasAnyRole('TRANSLATOR', 'ADMIN')")
    @PutMapping("/api/chapters/{chapterId}")
    public ResponseEntity<ChapterResponse> updateChapter(
            @PathVariable Long chapterId,
            @Valid @RequestBody ChapterRequest request) {
        return ResponseEntity.ok(chapterService.updateChapter(chapterId, request));
    }

    @PreAuthorize("hasAnyRole('TRANSLATOR', 'ADMIN')")
    @DeleteMapping("/api/chapters/{chapterId}")
    public ResponseEntity<String> deleteChapter(@PathVariable Long chapterId) {
        chapterService.deleteChapter(chapterId);
        return ResponseEntity.ok("Chapter deleted successfully with id: " + chapterId);
    }
}
