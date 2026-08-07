package com.comic.h.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.comic.h.dto.response.ChapterImageResponse;
import com.comic.h.entity.Chapter;
import com.comic.h.entity.ChapterImage;
import com.comic.h.entity.Comic;
import com.comic.h.entity.User;
import com.comic.h.exception.ForbiddenException;
import com.comic.h.repository.ChapterImageRepository;
import com.comic.h.repository.ChapterRepository;
import com.comic.h.security.ComicSecurityEvaluator;
import com.comic.h.service.impl.ChapterImageServiceImpl;
import com.comic.h.util.ImageProcessor;

@ExtendWith(MockitoExtension.class)
public class ChapterImageServiceTest {

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private ChapterImageRepository chapterImageRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ImageProcessor imageProcessor;

    private ComicSecurityEvaluator comicSecurityEvaluator = new ComicSecurityEvaluator();

    private ChapterImageServiceImpl chapterImageService;

    private Comic comicOwnedByTranslator;
    private Chapter chapter;
    private MockMultipartFile validImageFile;

    @BeforeEach
    public void setUp() throws IOException {
        chapterImageService = new ChapterImageServiceImpl(chapterRepository, chapterImageRepository, fileStorageService, imageProcessor, comicSecurityEvaluator);

        User uploader = new User();
        uploader.setUsername("translator1");

        comicOwnedByTranslator = Comic.builder()
                .id(1L)
                .title("Translator Comic")
                .slug("translator-comic")
                .uploader(uploader)
                .build();

        chapter = Chapter.builder()
                .id(10L)
                .chapterNumber(1.0)
                .title("Chapter 1")
                .comic(comicOwnedByTranslator)
                .build();

        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(img, "png", os);
        validImageFile = new MockMultipartFile("image", "page1.png", "image/png", os.toByteArray());

        lenient().when(imageProcessor.convertToWebp(any())).thenReturn(new byte[]{1, 2, 3});
        lenient().when(fileStorageService.saveFile(any(), any(), any())).thenReturn("upload/comic/translator-comic/translator-comic-chapter-1/page-001.webp");
    }

    private void authenticateUser(String username, String role) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                username, "password",
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void testUploadOrReplaceImage_NewPage_Success() {
        authenticateUser("translator1", "ROLE_TRANSLATOR");

        when(chapterRepository.findById(10L)).thenReturn(Optional.of(chapter));
        when(chapterImageRepository.findByChapterIdAndPageNumber(10L, 1)).thenReturn(Optional.empty());

        ChapterImageResponse response = chapterImageService.uploadOrReplaceImage(10L, validImageFile, 1);

        assertNotNull(response);
        assertEquals(1, response.getPageNumber());
        assertNotNull(response.getImageUrl());
        verify(chapterImageRepository).save(any(ChapterImage.class));
    }

    @Test
    public void testUploadOrReplaceImage_ReplaceExistingPage_Success() {
        authenticateUser("translator1", "ROLE_TRANSLATOR");

        ChapterImage existingImage = ChapterImage.builder()
                .id(100L)
                .chapter(chapter)
                .pageNumber(1)
                .imagePath("upload/comic/translator-comic/translator-comic-chapter-1/old.webp")
                .build();

        when(chapterRepository.findById(10L)).thenReturn(Optional.of(chapter));
        when(chapterImageRepository.findByChapterIdAndPageNumber(10L, 1)).thenReturn(Optional.of(existingImage));

        ChapterImageResponse response = chapterImageService.uploadOrReplaceImage(10L, validImageFile, 1);

        assertNotNull(response);
        assertEquals(1, response.getPageNumber());
        verify(chapterImageRepository).save(existingImage);
    }

    @Test
    public void testUploadImagesBatch_Success() {
        authenticateUser("translator1", "ROLE_TRANSLATOR");

        when(chapterRepository.findById(10L)).thenReturn(Optional.of(chapter));
        when(chapterImageRepository.findByChapterIdAndPageNumber(any(), any())).thenReturn(Optional.empty());

        List<ChapterImageResponse> responses = chapterImageService.uploadImagesBatch(
                10L, List.of(validImageFile, validImageFile), 1);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(1, responses.get(0).getPageNumber());
        assertEquals(2, responses.get(1).getPageNumber());
    }

    @Test
    public void testDeleteImageByPageNumber_Success() {
        authenticateUser("translator1", "ROLE_TRANSLATOR");

        ChapterImage existingImage = ChapterImage.builder()
                .id(100L)
                .chapter(chapter)
                .pageNumber(3)
                .imagePath("upload/comic/translator-comic/translator-comic-chapter-1/page-003.webp")
                .build();

        when(chapterRepository.findById(10L)).thenReturn(Optional.of(chapter));
        when(chapterImageRepository.findByChapterIdAndPageNumber(10L, 3)).thenReturn(Optional.of(existingImage));

        chapterImageService.deleteImageByPageNumber(10L, 3);

        verify(chapterImageRepository).deleteByChapterIdAndPageNumber(10L, 3);
    }

    @Test
    public void testDeleteAllImages_Success() {
        authenticateUser("translator1", "ROLE_TRANSLATOR");

        when(chapterRepository.findById(10L)).thenReturn(Optional.of(chapter));
        when(chapterImageRepository.findByChapterIdOrderByPageNumberAsc(10L)).thenReturn(Collections.emptyList());
        when(chapterImageRepository.deleteByChapterId(10L)).thenReturn(5L);

        long deletedCount = chapterImageService.deleteAllImages(10L);

        assertEquals(5L, deletedCount);
        verify(chapterImageRepository).deleteByChapterId(10L);
    }

    @Test
    public void testUnauthorizedUser_ThrowsForbidden() {
        authenticateUser("otherUser", "ROLE_TRANSLATOR");

        when(chapterRepository.findById(10L)).thenReturn(Optional.of(chapter));

        assertThrows(ForbiddenException.class, () -> {
            chapterImageService.uploadOrReplaceImage(10L, validImageFile, 1);
        });
    }
}
