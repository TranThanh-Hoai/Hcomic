package com.comic.h.comic.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.comic.h.comic.dto.request.ChapterRequest;
import com.comic.h.comic.dto.response.ChapterResponse;
import com.comic.h.comic.service.ChapterService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chapters/{chapterId}")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;

    @GetMapping("/status")
    public ResponseEntity<ChapterResponse> getChapterStatus(@PathVariable Long chapterId) {
        return ResponseEntity.ok(chapterService.getChapterById(chapterId));
    }

    @PreAuthorize("hasAnyRole('TRANSLATOR', 'ADMIN')")
    @PutMapping
    public ResponseEntity<ChapterResponse> updateChapter(
            @PathVariable Long chapterId,
            @Valid @RequestBody ChapterRequest request) {
        return ResponseEntity.ok(chapterService.updateChapter(chapterId, request));
    }

    @PreAuthorize("hasAnyRole('TRANSLATOR', 'ADMIN')")
    @DeleteMapping
    public ResponseEntity<String> deleteChapter(@PathVariable Long chapterId) {
        chapterService.deleteChapter(chapterId);
        return ResponseEntity.ok("Chapter deleted successfully with id: " + chapterId);
    }
}
