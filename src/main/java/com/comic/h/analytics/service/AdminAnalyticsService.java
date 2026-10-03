package com.comic.h.analytics.service;

import java.util.List;

import com.comic.h.analytics.dto.response.AdminOverviewResponse;
import com.comic.h.analytics.dto.response.TrendingComicResponse;
import com.comic.h.analytics.dto.response.UserGrowthPoint;

public interface AdminAnalyticsService {

    AdminOverviewResponse getOverview();

    List<TrendingComicResponse> getTrendingComics(String period, int limit);
    
    List<UserGrowthPoint> getUserGrowth(int days);
} 