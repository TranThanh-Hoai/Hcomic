package com.comic.h.common.exception;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Redis GET error on cache '{}' with key '{}': {}. Falling back to database.",
                cache != null ? cache.getName() : "unknown", key, exception.getMessage());
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.warn("Redis PUT error on cache '{}' with key '{}': {}. Continuing without caching.",
                cache != null ? cache.getName() : "unknown", key, exception.getMessage());
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Redis EVICT error on cache '{}' with key '{}': {}. Continuing without cache eviction.",
                cache != null ? cache.getName() : "unknown", key, exception.getMessage());
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.warn("Redis CLEAR error on cache '{}': {}. Continuing without cache clearing.",
                cache != null ? cache.getName() : "unknown", exception.getMessage());
    }
}
