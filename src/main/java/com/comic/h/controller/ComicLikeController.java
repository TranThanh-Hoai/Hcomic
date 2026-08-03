package com.comic.h.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.comic.h.dto.response.ComicLikeResponse;
import com.comic.h.service.ComicLikeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/comics/{comicId}")
@RequiredArgsConstructor
public class ComicLikeController {

    private final ComicLikeService comicLikeService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/toggle-like")
    public ResponseEntity<ComicLikeResponse> toggleLike(@PathVariable Long comicId, Authentication authentication) {
        ComicLikeResponse response = comicLikeService.toggleLike(comicId, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/like/status")
    public ResponseEntity<ComicLikeResponse> getLikeStatus(@PathVariable Long comicId, Authentication authentication) {
        ComicLikeResponse response = comicLikeService.getLikeStatus(comicId, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
