package com.comic.h.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.comic.h.dto.response.AdminOverviewResponse;
import com.comic.h.dto.response.TrendingComicResponse;
import com.comic.h.dto.response.UserGrowthPoint;
import com.comic.h.service.AdminAnalyticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    private final AdminAnalyticsService analyticsService;

    @GetMapping("/overview")
    public ResponseEntity<AdminOverviewResponse> getOverview() {
        return ResponseEntity.ok(analyticsService.getOverview());
    }

    @GetMapping("/trending")
    public ResponseEntity<List<TrendingComicResponse>> getTrendingComics(
            @RequestParam(defaultValue = "WEEK") String period,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTrendingComics(period, limit));
    }

    @GetMapping("/user-growth")
    public ResponseEntity<List<UserGrowthPoint>> getUserGrowth(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(analyticsService.getUserGrowth(days));
    }
}
