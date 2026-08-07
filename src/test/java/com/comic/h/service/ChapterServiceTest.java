package com.comic.h.service;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.comic.h.dto.request.ChapterRequest;
import com.comic.h.entity.Chapter;
import com.comic.h.entity.Comic;
import com.comic.h.exception.ForbiddenException;
import com.comic.h.repository.ChapterRepository;
import com.comic.h.repository.ComicRepository;
import com.comic.h.service.impl.ChapterServiceImpl;

@ExtendWith(MockitoExtension.class)
public class ChapterServiceTest {

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private ComicRepository comicRepository;

    private ChapterServiceImpl chapterService;

    private Comic comicOwnedByTranslator1;

    @BeforeEach
    public void setUp() {
        chapterService = new ChapterServiceImpl(chapterRepository, comicRepository);

        com.comic.h.entity.User uploader = new com.comic.h.entity.User();
        uploader.setUsername("translator1");

        comicOwnedByTranslator1 = Comic.builder()
                .id(1L)
                .title("Translator 1 Comic")
                .uploader(uploader)
                .build();
    }

    @Test
    public void testTranslatorCanModifyOwnComicChapter() {
        // Set security context for translator1
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "translator1", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRANSLATOR"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        Chapter chapter = Chapter.builder()
                .id(10L)
                .chapterNumber(1.0)
                .comic(comicOwnedByTranslator1)
                .build();

        when(chapterRepository.findById(10L)).thenReturn(Optional.of(chapter));

        // Call deleteChapter
        chapterService.deleteChapter(10L);

        verify(chapterRepository).delete(chapter);
    }

    @Test
    public void testTranslatorCannotModifyAnotherTranslatorComicChapter() {
        // Set security context for translator2
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "translator2", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRANSLATOR"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        Chapter chapter = Chapter.builder()
                .id(10L)
                .chapterNumber(1.0)
                .comic(comicOwnedByTranslator1)
                .build();

        when(chapterRepository.findById(10L)).thenReturn(Optional.of(chapter));

        // Attempt deleteChapter should throw ForbiddenException
        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> {
            chapterService.deleteChapter(10L);
        });

        assertEquals("You do not have permission to modify chapters for this comic", ex.getMessage());
        verify(chapterRepository, never()).delete(any());
    }

    @Test
    public void testAdminCanModifyAnyComicChapter() {
        // Set security context for admin
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "adminUser", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        Chapter chapter = Chapter.builder()
                .id(10L)
                .chapterNumber(1.0)
                .comic(comicOwnedByTranslator1)
                .build();

        when(chapterRepository.findById(10L)).thenReturn(Optional.of(chapter));

        // Admin calling deleteChapter
        chapterService.deleteChapter(10L);

        verify(chapterRepository).delete(chapter);
    }

    @Test
    public void testUserCannotModifyComicChapter_ThrowsForbidden() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "normalUser", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        Chapter chapter = Chapter.builder()
                .id(10L)
                .chapterNumber(1.0)
                .comic(comicOwnedByTranslator1)
                .build();

        when(chapterRepository.findById(10L)).thenReturn(Optional.of(chapter));

        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> {
            chapterService.deleteChapter(10L);
        });

        assertEquals("You do not have permission to modify chapters for this comic", ex.getMessage());
        verify(chapterRepository, never()).delete(any());
    }

    @Test
    public void testGetChapterDetailBySlug_IncrementsChapterAndComicViewCount() {
        Chapter chapter = Chapter.builder()
                .id(100L)
                .chapterNumber(1.0)
                .slug("chuong-1")
                .viewCount(5L)
                .comic(comicOwnedByTranslator1)
                .images(Collections.emptyList())
                .build();

        when(chapterRepository.findByComicSlugAndSlug("translator-1-comic", "chuong-1"))
                .thenReturn(Optional.of(chapter));

        var response = chapterService.getChapterDetailBySlug("translator-1-comic", "chuong-1");

        assertEquals(100L, response.getId());
        verify(chapterRepository).incrementViewCount(100L);
        verify(comicRepository).incrementViewCount(1L);
    }
}

