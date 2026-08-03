package com.comic.h.controller;

import org.springframework.http.ResponseEntity;
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

    @PostMapping("/toggle-like")
    public ResponseEntity<?> toggleLike(@PathVariable Long comicId, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body("User must be authenticated to toggle like on a comic");
            }
            ComicLikeResponse response = comicLikeService.toggleLike(comicId, authentication.getName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/like/status")
    public ResponseEntity<?> getLikeStatus(@PathVariable Long comicId, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body("User must be authenticated");
            }
            ComicLikeResponse response = comicLikeService.getLikeStatus(comicId, authentication.getName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
