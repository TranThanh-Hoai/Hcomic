package com.comic.h.common.cache;

import java.util.Collection;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Tiện ích hỗ trợ evict Redis Cache an toàn:
 * 1. Chỉ thực hiện evict sau khi Database Transaction đã commit thành công (After-Commit).
 * 2. Bọc try-catch toàn bộ thao tác evict/clear để nếu Redis gặp sự cố, luồng nghiệp vụ DB vẫn an toàn.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SafeCacheEvictor {

    private final CacheManager cacheManager;

    /**
     * Xóa 1 key trong cache sau khi transaction hiện tại commit thành công.
     * Nếu không trong transaction, thực hiện xóa ngay lập tức.
     */
    public void evictAfterCommit(String cacheName, Object key) {
        if (key == null) {
            return;
        }
        executeAfterCommit(() -> evictNow(cacheName, key));
    }

    /**
     * Xóa danh sách các key trong cache sau khi transaction commit thành công.
     */
    public void evictMultipleAfterCommit(String cacheName, Collection<?> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        executeAfterCommit(() -> {
            for (Object key : keys) {
                if (key != null) {
                    evictNow(cacheName, key);
                }
            }
        });
    }

    /**
     * Xóa toàn bộ dữ liệu trong cache sau khi transaction commit thành công.
     */
    public void clearAfterCommit(String cacheName) {
        executeAfterCommit(() -> clearNow(cacheName));
    }

    /**
     * Thực hiện xóa 1 key ngay lập tức và bọc an toàn trong try-catch.
     */
    public void evictNow(String cacheName, Object key) {
        if (key == null || cacheManager == null) {
            return;
        }
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(key);
                log.debug("Safely evicted key '{}' from cache '{}'", key, cacheName);
            }
        } catch (Exception e) {
            log.warn("Redis safe evict failed for cache '{}' and key '{}': {}. Continuing without failing business transaction.",
                    cacheName, key, e.getMessage());
        }
    }

    /**
     * Thực hiện xóa sạch toàn bộ cache ngay lập tức và bọc an toàn trong try-catch.
     */
    public void clearNow(String cacheName) {
        if (cacheManager == null) {
            return;
        }
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.debug("Safely cleared cache '{}'", cacheName);
            }
        } catch (Exception e) {
            log.warn("Redis safe clear failed for cache '{}': {}. Continuing without failing business transaction.",
                    cacheName, e.getMessage());
        }
    }

    private void executeAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == TransactionSynchronization.STATUS_COMMITTED) {
                        action.run();
                    } else {
                        log.debug("Transaction rolled back or failed (status={}), skipping cache eviction.", status);
                    }
                }
            });
        } else {
            action.run();
        }
    }
}
