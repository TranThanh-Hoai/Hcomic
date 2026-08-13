package com.comic.h.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.comic.h.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenScheduler {

    private final RefreshTokenService refreshTokenService;

    // Run every day at 3:00 AM
    @Scheduled(cron = "0 0 3 * * ?")
    public void purgeExpiredTokens() {
        log.info("Running scheduled task: Purging expired refresh tokens...");
        int count = refreshTokenService.deleteExpiredTokens();
        log.info("Finished scheduled task: {} expired tokens purged.", count);
    }
}
