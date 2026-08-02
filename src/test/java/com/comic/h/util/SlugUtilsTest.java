package com.comic.h.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SlugUtilsTest {

    @Test
    @DisplayName("toSlug - Chuyển đổi chuỗi thường thành slug")
    void toSlug_NormalString() {
        assertEquals("one-piece", SlugUtils.toSlug("One Piece"));
    }

    @Test
    @DisplayName("toSlug - Chuyển đổi ký tự tiếng Việt ă, á, ạ, đ thành a, d")
    void toSlug_VietnameseAccents() {
        assertEquals("a-a-a", SlugUtils.toSlug("ă, á, ạ"));
        assertEquals("truyen-tranh-dac-nhan-tam", SlugUtils.toSlug("Truyện Tranh Đắc Nhân Tâm"));
        assertEquals("do-re-mon-ngoi-den-co", SlugUtils.toSlug("Đô-rê-mon: Ngôi Đền Cổ"));
    }

    @Test
    @DisplayName("toSlug - Xử lý khoảng trắng và ký tự đặc biệt")
    void toSlug_SpecialCharacters() {
        assertEquals("hello-world", SlugUtils.toSlug("  Hello @#$ World!  "));
    }

    @Test
    @DisplayName("toSlug - Xử lý chuỗi null hoặc rỗng")
    void toSlug_NullOrEmpty() {
        assertEquals("", SlugUtils.toSlug(null));
        assertEquals("", SlugUtils.toSlug("   "));
    }
}
