package com.comic.h.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import com.comic.h.dto.request.ComicRequest;
import com.comic.h.dto.response.ComicResponse;
import com.comic.h.entity.Comic;
import com.comic.h.entity.User;
import com.comic.h.exception.ForbiddenException;
import com.comic.h.repository.ComicRepository;
import com.comic.h.repository.UserRepository;
import com.comic.h.security.ComicSecurityEvaluator;
import com.comic.h.service.impl.ComicServiceImpl;
import com.comic.h.util.ImageProcessor;

@ExtendWith(MockitoExtension.class)
public class ComicServiceTest {

    @Mock
    private ComicRepository comicRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ImageProcessor imageProcessor;

    private ComicSecurityEvaluator comicSecurityEvaluator = new ComicSecurityEvaluator();

    private ComicServiceImpl comicService;

    private Comic comicOwnedByTranslator1;

    @BeforeEach
    public void setUp() {
        comicService = new ComicServiceImpl(comicRepository, userRepository, fileStorageService, imageProcessor, comicSecurityEvaluator);

        User uploader = new User();
        uploader.setUsername("translator1");

        comicOwnedByTranslator1 = Comic.builder()
                .id(1L)
                .title("Translator 1 Comic")
                .slug("translator-1-comic")
                .uploader(uploader)
                .build();
    }

    @Test
    public void testGetMyComics_Success() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "translator1", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRANSLATOR"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(comicRepository.findByUploaderUsernameOrderByCreatedAtDesc("translator1"))
                .thenReturn(List.of(comicOwnedByTranslator1));

        List<ComicResponse> response = comicService.getMyComics();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Translator 1 Comic", response.get(0).getTitle());
        verify(comicRepository).findByUploaderUsernameOrderByCreatedAtDesc("translator1");
    }

    @Test
    public void testGetComicsByUploader_Success() {
        when(comicRepository.findByUploaderUsernameOrderByCreatedAtDesc("translator1"))
                .thenReturn(List.of(comicOwnedByTranslator1));

        List<ComicResponse> response = comicService.getComicsByUploader("translator1");

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("translator1", response.get(0).getUploader());
    }

    @Test
    public void testUpdateComic_Owner_Success() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "translator1", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRANSLATOR"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(comicRepository.findById(1L)).thenReturn(Optional.of(comicOwnedByTranslator1));
        when(comicRepository.save(any(Comic.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ComicRequest request = new ComicRequest();
        request.setTitle("Updated Title");

        ComicResponse response = comicService.updateComic(1L, request, null);

        assertNotNull(response);
        assertEquals("Updated Title", response.getTitle());
        verify(comicRepository).save(comicOwnedByTranslator1);
    }

    @Test
    public void testUpdateComic_OtherTranslator_ThrowsForbidden() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "translator2", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRANSLATOR"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(comicRepository.findById(1L)).thenReturn(Optional.of(comicOwnedByTranslator1));

        ComicRequest request = new ComicRequest();
        request.setTitle("Updated Title");

        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> {
            comicService.updateComic(1L, request, null);
        });

        assertEquals("You do not have permission to perform action on this comic", ex.getMessage());
        verify(comicRepository, never()).save(any());
    }

    @Test
    public void testDeleteComic_Owner_Success() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "translator1", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRANSLATOR"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(comicRepository.findById(1L)).thenReturn(Optional.of(comicOwnedByTranslator1));

        comicService.deleteComic(1L);

        verify(comicRepository).delete(comicOwnedByTranslator1);
    }

    @Test
    public void testDeleteComic_OtherTranslator_ThrowsForbidden() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "translator2", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRANSLATOR"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(comicRepository.findById(1L)).thenReturn(Optional.of(comicOwnedByTranslator1));

        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> {
            comicService.deleteComic(1L);
        });

        assertEquals("You do not have permission to perform action on this comic", ex.getMessage());
        verify(comicRepository, never()).delete(any());
    }

    @Test
    public void testDeleteComic_Admin_Success() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "adminUser", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(comicRepository.findById(1L)).thenReturn(Optional.of(comicOwnedByTranslator1));

        comicService.deleteComic(1L);

        verify(comicRepository).delete(comicOwnedByTranslator1);
    }

    @Test
    public void testUpdateComic_UserRole_ThrowsForbidden() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "normalUser", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(comicRepository.findById(1L)).thenReturn(Optional.of(comicOwnedByTranslator1));

        ComicRequest request = new ComicRequest();
        request.setTitle("Updated Title");

        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> {
            comicService.updateComic(1L, request, null);
        });

        assertEquals("You do not have permission to perform action on this comic", ex.getMessage());
        verify(comicRepository, never()).save(any());
    }
}
