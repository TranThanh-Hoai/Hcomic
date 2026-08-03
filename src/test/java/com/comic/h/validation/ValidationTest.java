package com.comic.h.validation;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.comic.h.dto.request.ChapterRequest;
import com.comic.h.dto.request.ComicRateRequest;
import com.comic.h.dto.request.ComicRequest;
import com.comic.h.dto.request.CommentRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class ValidationTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testChapterRequestValidation() {
        // Valid
        ChapterRequest valid = ChapterRequest.builder()
                .chapterNumber(1.5)
                .title("Valid Title")
                .build();
        Set<ConstraintViolation<ChapterRequest>> violations = validator.validate(valid);
        assertTrue(violations.isEmpty());

        // Invalid: chapterNumber <= 0
        ChapterRequest invalidNumber = ChapterRequest.builder()
                .chapterNumber(0.0)
                .title("Valid Title")
                .build();
        violations = validator.validate(invalidNumber);
        assertFalse(violations.isEmpty());

        // Invalid: title too long
        ChapterRequest invalidTitle = ChapterRequest.builder()
                .chapterNumber(1.0)
                .title("a".repeat(256))
                .build();
        violations = validator.validate(invalidTitle);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testComicRequestValidation() {
        // Valid
        ComicRequest valid = ComicRequest.builder()
                .title("My Comic")
                .description("Good description")
                .author("Author")
                .build();
        Set<ConstraintViolation<ComicRequest>> violations = validator.validate(valid);
        assertTrue(violations.isEmpty());

        // Invalid: empty title
        ComicRequest emptyTitle = ComicRequest.builder()
                .title("   ")
                .build();
        violations = validator.validate(emptyTitle);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testComicRateRequestValidation() {
        // Valid
        ComicRateRequest valid = ComicRateRequest.builder()
                .comicId(1L)
                .rating(4.5)
                .build();
        Set<ConstraintViolation<ComicRateRequest>> violations = validator.validate(valid);
        assertTrue(violations.isEmpty());

        // Invalid: rating out of bounds
        ComicRateRequest invalidRating = ComicRateRequest.builder()
                .comicId(1L)
                .rating(6.0)
                .build();
        violations = validator.validate(invalidRating);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testCommentRequestValidation() {
        // Valid
        CommentRequest valid = CommentRequest.builder()
                .content("Great chapter!")
                .chapterId(10L)
                .build();
        Set<ConstraintViolation<CommentRequest>> violations = validator.validate(valid);
        assertTrue(violations.isEmpty());

        // Invalid: empty content
        CommentRequest invalidContent = CommentRequest.builder()
                .content("   ")
                .build();
        violations = validator.validate(invalidContent);
        assertFalse(violations.isEmpty());
    }
}
