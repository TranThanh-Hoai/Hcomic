package com.comic.h.common.cache;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

class SafeCacheEvictorTest {

    private CacheManager cacheManager;
    private Cache cache;
    private SafeCacheEvictor evictor;

    @BeforeEach
    void setUp() {
        cacheManager = mock(CacheManager.class);
        cache = mock(Cache.class);
        evictor = new SafeCacheEvictor(cacheManager);
    }

    @Test
    void testEvictNowSuccess() {
        when(cacheManager.getCache("comic_detail")).thenReturn(cache);

        evictor.evictNow("comic_detail", "dau-pha-thuong-khung");

        verify(cache).evict("dau-pha-thuong-khung");
    }

    @Test
    void testEvictNowHandlesExceptionGracefully() {
        when(cacheManager.getCache("comic_detail")).thenReturn(cache);
        doThrow(new RuntimeException("Redis connection failure")).when(cache).evict(any());

        // Đảm bảo không ném ngoại lệ làm sập nghiệp vụ
        assertDoesNotThrow(() -> evictor.evictNow("comic_detail", "dau-pha-thuong-khung"));
    }

    @Test
    void testClearNowHandlesExceptionGracefully() {
        when(cacheManager.getCache("comics_page")).thenReturn(cache);
        doThrow(new RuntimeException("Redis timeout")).when(cache).clear();

        assertDoesNotThrow(() -> evictor.clearNow("comics_page"));
    }

    @Test
    void testEvictMultipleAfterCommitWithoutActiveTransaction() {
        when(cacheManager.getCache("chapters_list")).thenReturn(cache);

        evictor.evictMultipleAfterCommit("chapters_list", List.of("slug:asc", "slug:desc"));

        verify(cache).evict("slug:asc");
        verify(cache).evict("slug:desc");
    }
}
