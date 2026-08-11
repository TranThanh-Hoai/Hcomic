package com.comic.h.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOverviewResponse {
    private long totalUsers;
    private long totalComics;
    private long totalReads;
    private long newUsersToday;
    private long newUsersThisWeek;
    private long pendingReportsCount;
    private long bannedUsersCount;
}
