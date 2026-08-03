package com.comic.h.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.comic.h.dto.request.ComicRateRequest;
import com.comic.h.dto.response.ComicRateResponse;
import com.comic.h.service.ComicRateService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class ComicRateController {

    private final ComicRateService ratingService;

    @PostMapping
    public ResponseEntity<?> rateComic(@RequestBody ComicRateRequest request, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body("User must be authenticated to rate");
            }
            ComicRateResponse response = ratingService.rateComic(request, authentication.getName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/comic/{comicId}/average")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long comicId) {
        return ResponseEntity.ok(ratingService.getAverageRating(comicId));
    }

    @GetMapping("/comic/{comicId}/user")
    public ResponseEntity<?> getUserRating(@PathVariable Long comicId, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body("User must be authenticated");
            }
            ComicRateResponse response = ratingService.getUserRatingForComic(comicId, authentication.getName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
