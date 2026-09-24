package com.comic.h.common.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import com.comic.h.comic.dto.response.ComicResponse;
import com.comic.h.comic.dto.response.GenreResponse;
import com.comic.h.comic.enums.ComicStatus;
import com.comic.h.common.dto.response.PageResponse;

@SpringBootTest
class RedisConfigTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testRedisSerializationWithLocalDateTimeAndGenericPage() {
        String testKey = "test:comic:1";

        ComicResponse comic = ComicResponse.builder()
                .id(1L)
                .title("Đấu Phá Thương Khung")
                .slug("dau-pha-thuong-khung")
                .author("Thiên Tằm Thổ Đậu")
                .status(ComicStatus.ONGOING)
                .viewCount(9999L)
                .genres(List.of(GenreResponse.builder().id(1L).name("Tiên Hiệp").slug("tien-hiep").build()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PageResponse<ComicResponse> pageResponse = PageResponse.<ComicResponse>builder()
                .content(List.of(comic))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .build();

        // 1. Lưu vào Redis
        redisTemplate.opsForValue().set(testKey, pageResponse);

        // 2. Đọc lại từ Redis
        Object cachedObj = redisTemplate.opsForValue().get(testKey);
        assertNotNull(cachedObj);

        // 3. Ép kiểu và kiểm chứng không bị lỗi ClassCastException hoặc crash LocalDateTime
        @SuppressWarnings("unchecked")
        PageResponse<ComicResponse> retrieved = (PageResponse<ComicResponse>) cachedObj;
        assertNotNull(retrieved.getContent());
        assertEquals(1, retrieved.getContent().size());

        ComicResponse cachedComic = retrieved.getContent().get(0);
        assertEquals("Đấu Phá Thương Khung", cachedComic.getTitle());
        assertNotNull(cachedComic.getCreatedAt());

        // Dọn dẹp key test
        redisTemplate.delete(testKey);
    }
}
