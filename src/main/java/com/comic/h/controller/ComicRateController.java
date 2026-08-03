package com.comic.h.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class ComicRateController {

    private final ComicRateService ratingService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<ComicRateResponse> rateComic(@Valid @RequestBody ComicRateRequest request, Authentication authentication) {
        ComicRateResponse response = ratingService.rateComic(request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/comic/{comicId}/average")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long comicId) {
        return ResponseEntity.ok(ratingService.getAverageRating(comicId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/comic/{comicId}/user")
    public ResponseEntity<ComicRateResponse> getUserRating(@PathVariable Long comicId, Authentication authentication) {
        ComicRateResponse response = ratingService.getUserRatingForComic(comicId, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
