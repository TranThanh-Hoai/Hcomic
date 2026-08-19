package com.comic.h.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comic.h.dto.response.AdminOverviewResponse;
import com.comic.h.dto.response.TrendingComicResponse;
import com.comic.h.dto.response.UserGrowthPoint;
import com.comic.h.entity.Comic;
import com.comic.h.enums.ReportStatus;
import com.comic.h.repository.ComicRepository;
import com.comic.h.repository.ReportRepository;
import com.comic.h.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAnalyticsService {

    private final UserRepository userRepository;
    private final ComicRepository comicRepository;
    private final ReportRepository reportRepository;

    public AdminOverviewResponse getOverview() {
        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime startOfWeek = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.MIN);

        long totalUsers = userRepository.count();
        long totalComics = comicRepository.count();
        long totalReads = comicRepository.sumTotalViewCount();
        long newUsersToday = userRepository.countByCreatedAtAfter(startOfToday);
        long newUsersThisWeek = userRepository.countByCreatedAtAfter(startOfWeek);
        long pendingReportsCount = reportRepository.countByStatus(ReportStatus.PENDING);
        long bannedUsersCount = userRepository.countByIsBannedTrue();

        return AdminOverviewResponse.builder()
                .totalUsers(totalUsers)
                .totalComics(totalComics)
                .totalReads(totalReads)
                .newUsersToday(newUsersToday)
                .newUsersThisWeek(newUsersThisWeek)
                .pendingReportsCount(pendingReportsCount)
                .bannedUsersCount(bannedUsersCount)
                .build();
    }

    public List<TrendingComicResponse> getTrendingComics(String period, int limit) {
        LocalDateTime sinceDate;
        if ("DAY".equalsIgnoreCase(period)) {
            sinceDate = LocalDateTime.now().minusDays(1);
        } else if ("MONTH".equalsIgnoreCase(period)) {
            sinceDate = LocalDateTime.now().minusDays(30);
        } else {
            // Default WEEK
            sinceDate = LocalDateTime.now().minusDays(7);
        }

        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> rawResults = comicRepository.findTrendingComicsSince(sinceDate, pageable);

        List<TrendingComicResponse> responses = new ArrayList<>();
        for (Object[] row : rawResults) {
            Comic comic = (Comic) row[0];
            Long count = (Long) row[1];

            responses.add(TrendingComicResponse.builder()
                    .comicId(comic.getId())
                    .title(comic.getTitle())
                    .slug(comic.getSlug())
                    .coverImage(comic.getCoverImage())
                    .author(comic.getAuthor())
                    .readCountInPeriod(count != null ? count : 0L)
                    .totalViewCount(comic.getViewCount() != null ? comic.getViewCount() : 0L)
                    .avgRating(comic.getAvgRating() != null ? comic.getAvgRating() : 0.0)
                    .build());
        }

        // If not enough history data, fallback to top comics by total viewCount
        if (responses.size() < limit) {
            Pageable fallbackPageable = PageRequest.of(0, limit, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "viewCount", "createdAt"));
            List<Comic> topComics = comicRepository.findAll(fallbackPageable).getContent();
            for (Comic c : topComics) {
                boolean exists = responses.stream().anyMatch(r -> r.getComicId().equals(c.getId()));
                if (!exists) {
                    responses.add(TrendingComicResponse.builder()
                            .comicId(c.getId())
                            .title(c.getTitle())
                            .slug(c.getSlug())
                            .coverImage(c.getCoverImage())
                            .author(c.getAuthor())
                            .readCountInPeriod(0L)
                            .totalViewCount(c.getViewCount() != null ? c.getViewCount() : 0L)
                            .avgRating(c.getAvgRating() != null ? c.getAvgRating() : 0.0)
                            .build());
                }
            }
        }

        return responses;
    }

    public List<UserGrowthPoint> getUserGrowth(int days) {
        int validDays = Math.max(days, 1);
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(validDays - 1);
        LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.MIN);

        List<Object[]> rawCounts = userRepository.countUsersGroupedByDate(startDateTime);
        java.util.Map<String, Long> dateCountMap = new java.util.HashMap<>();
        for (Object[] row : rawCounts) {
            if (row != null && row.length >= 2 && row[0] != null) {
                String dateStr = row[0].toString();
                Long count = ((Number) row[1]).longValue();
                dateCountMap.put(dateStr, count);
            }
        }

        List<UserGrowthPoint> growth = new ArrayList<>();
        for (int i = validDays - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.toString();
            long count = dateCountMap.getOrDefault(dateStr, 0L);

            growth.add(UserGrowthPoint.builder()
                    .date(dateStr)
                    .count(count)
                    .build());
        }

        return growth;
    }
}
