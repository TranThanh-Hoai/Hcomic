package com.comic.h.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.comic.h.dto.request.PageBookmarkRequest;
import com.comic.h.dto.response.PageBookmarkResponse;
import com.comic.h.service.PageBookmarkService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class PageBookmarkController {

    private final PageBookmarkService pageBookmarkService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<PageBookmarkResponse> createOrUpdateBookmark(
            @Valid @RequestBody PageBookmarkRequest request,
            Authentication authentication) {
        PageBookmarkResponse response = pageBookmarkService.createOrUpdateBookmark(request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<PageBookmarkResponse>> getBookmarks(
            @RequestParam(required = false) Long comicId,
            @RequestParam(required = false) Long chapterId,
            Authentication authentication) {
        List<PageBookmarkResponse> bookmarks;
        if (chapterId != null) {
            bookmarks = pageBookmarkService.getBookmarksByChapter(chapterId, authentication.getName());
        } else if (comicId != null) {
            bookmarks = pageBookmarkService.getBookmarksByComic(comicId, authentication.getName());
        } else {
            bookmarks = pageBookmarkService.getUserBookmarks(authentication.getName());
        }
        return ResponseEntity.ok(bookmarks);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<Void> deleteBookmark(
            @PathVariable Long bookmarkId,
            Authentication authentication) {
        pageBookmarkService.deleteBookmark(bookmarkId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
