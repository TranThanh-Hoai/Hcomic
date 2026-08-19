package com.comic.h.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import com.comic.h.dto.request.ComicRequest;
import com.comic.h.dto.response.ComicResponse;
import com.comic.h.entity.Comic;
import com.comic.h.entity.Genre;
import com.comic.h.entity.User;
import com.comic.h.enums.ComicStatus;
import com.comic.h.enums.Role;
import com.comic.h.exception.ForbiddenException;
import com.comic.h.exception.ResourceNotFoundException;
import com.comic.h.mapper.ComicMapper;
import com.comic.h.repository.ChapterImageRepository;
import com.comic.h.repository.ComicRepository;
import com.comic.h.repository.GenreRepository;
import com.comic.h.repository.UserRepository;
import com.comic.h.security.ComicSecurityEvaluator;
import com.comic.h.service.FileStorageService;
import com.comic.h.util.ImageProcessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComicServiceImplTest {

    @Mock
    private ComicRepository comicRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private ChapterImageRepository chapterImageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ImageProcessor imageProcessor;

    @Mock
    private ComicSecurityEvaluator comicSecurityEvaluator;

    @Mock
    private ComicMapper comicMapper;

    @InjectMocks
    private ComicServiceImpl comicService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityUser(String username) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ==========================================
    // 1. CREATE COMIC TESTS
    // ==========================================

    @Test
    @DisplayName("Create Comic - Thành công với đầy đủ cover và thể loại")
    void createComic_Success() throws IOException {
        // Arrange
        mockSecurityUser("uploader_user");

        User uploader = User.builder()
                .userId(1L)
                .username("uploader_user")
                .role(Role.USER)
                .build();
        when(userRepository.findByUsername("uploader_user")).thenReturn(Optional.of(uploader));

        ComicRequest request = ComicRequest.builder()
                .title("Solo Leveling")
                .description("Action comic description")
                .author("Chugong")
                .status(ComicStatus.ONGOING)
                .genreIds(List.of(1L, 2L))
                .build();

        MockMultipartFile coverFile = new MockMultipartFile(
                "cover", "cover.png", "image/png", new byte[]{1, 2, 3}
        );

        Genre genre1 = Genre.builder().id(1L).name("Action").slug("action").build();
        Genre genre2 = Genre.builder().id(2L).name("Fantasy").slug("fantasy").build();
        when(genreRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(genre1, genre2));

        when(imageProcessor.convertToWebp(any(MultipartFile.class))).thenReturn(new byte[]{4, 5, 6});
        when(fileStorageService.saveFile(any(byte[].class), anyString(), anyString()))
                .thenReturn("upload/comic/solo-leveling/solo-leveling-cover.webp");

        when(comicRepository.existsBySlug("solo-leveling")).thenReturn(false);

        Comic savedComic = Comic.builder()
                .id(100L)
                .title("Solo Leveling")
                .slug("solo-leveling")
                .description("Action comic description")
                .author("Chugong")
                .uploader(uploader)
                .coverImage("upload/comic/solo-leveling/solo-leveling-cover.webp")
                .status(ComicStatus.ONGOING)
                .genres(Set.of(genre1, genre2))
                .build();
        when(comicRepository.save(any(Comic.class))).thenReturn(savedComic);

        ComicResponse expectedResponse = ComicResponse.builder()
                .id(100L)
                .title("Solo Leveling")
                .slug("solo-leveling")
                .uploader("uploader_user")
                .coverImage("upload/comic/solo-leveling/solo-leveling-cover.webp")
                .status(ComicStatus.ONGOING)
                .build();
        when(comicMapper.toResponse(savedComic)).thenReturn(expectedResponse);

        // Act
        ComicResponse actualResponse = comicService.createComic(request, coverFile);

        // Assert
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getId()).isEqualTo(100L);
        assertThat(actualResponse.getTitle()).isEqualTo("Solo Leveling");
        assertThat(actualResponse.getUploader()).isEqualTo("uploader_user");

        verify(comicRepository).save(any(Comic.class));
        verify(imageProcessor).convertToWebp(coverFile);
        verify(fileStorageService).saveFile(any(byte[].class), anyString(), anyString());
    }

    @Test
    @DisplayName("Create Comic - Ném ResourceNotFoundException khi uploader không tồn tại")
    void createComic_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        mockSecurityUser("unknown_user");
        when(userRepository.findByUsername("unknown_user")).thenReturn(Optional.empty());

        ComicRequest request = ComicRequest.builder()
                .title("Solo Leveling")
                .build();

        // Act & Assert
        assertThatThrownBy(() -> comicService.createComic(request, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with username: unknown_user");

        verify(comicRepository, never()).save(any(Comic.class));
    }

    // ==========================================
    // 2. READ COMIC TESTS
    // ==========================================

    @Test
    @DisplayName("Get Comic By ID - Thành công khi ID tồn tại")
    void getComicById_Success() {
        // Arrange
        Long comicId = 1L;
        Comic comic = Comic.builder()
                .id(comicId)
                .title("One Piece")
                .slug("one-piece")
                .build();
        when(comicRepository.findById(comicId)).thenReturn(Optional.of(comic));

        ComicResponse expectedResponse = ComicResponse.builder()
                .id(comicId)
                .title("One Piece")
                .slug("one-piece")
                .build();
        when(comicMapper.toResponse(comic)).thenReturn(expectedResponse);

        // Act
        ComicResponse actualResponse = comicService.getComicById(comicId);

        // Assert
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getId()).isEqualTo(comicId);
        assertThat(actualResponse.getTitle()).isEqualTo("One Piece");
        verify(comicRepository).findById(comicId);
    }

    @Test
    @DisplayName("Get Comic By ID - Ném ResourceNotFoundException khi ID không tồn tại")
    void getComicById_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(comicRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> comicService.getComicById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Comic not found with id: 999");
    }

    // ==========================================
    // 3. UPDATE COMIC & AUTHORIZATION TESTS
    // ==========================================

    @Test
    @DisplayName("Update Comic - Thành công bởi chủ sở hữu (Owner)")
    void updateComic_Success_ByOwner() {
        // Arrange
        Long comicId = 1L;
        Comic comic = Comic.builder()
                .id(comicId)
                .title("Old Title")
                .slug("old-title")
                .description("Old Desc")
                .status(ComicStatus.ONGOING)
                .build();

        when(comicRepository.findById(comicId)).thenReturn(Optional.of(comic));
        doNothing().when(comicSecurityEvaluator).verifyOwnership(comic);

        ComicRequest updateRequest = ComicRequest.builder()
                .description("New Description")
                .status(ComicStatus.COMPLETED)
                .build();

        when(comicRepository.save(comic)).thenReturn(comic);

        ComicResponse expectedResponse = ComicResponse.builder()
                .id(comicId)
                .title("Old Title")
                .description("New Description")
                .status(ComicStatus.COMPLETED)
                .build();
        when(comicMapper.toResponse(comic)).thenReturn(expectedResponse);

        // Act
        ComicResponse response = comicService.updateComic(comicId, updateRequest, null);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getDescription()).isEqualTo("New Description");
        assertThat(response.getStatus()).isEqualTo(ComicStatus.COMPLETED);

        verify(comicSecurityEvaluator).verifyOwnership(comic);
        verify(comicRepository).save(comic);
    }

    @Test
    @DisplayName("Update Comic - Ném ForbiddenException khi không phải chủ sở hữu hoặc admin")
    void updateComic_Forbidden_WhenNotOwnerOrAdmin() {
        // Arrange
        Long comicId = 1L;
        Comic comic = Comic.builder()
                .id(comicId)
                .title("Original Comic")
                .build();

        when(comicRepository.findById(comicId)).thenReturn(Optional.of(comic));
        doThrow(new ForbiddenException("You do not have permission to perform action on this comic"))
                .when(comicSecurityEvaluator).verifyOwnership(comic);

        ComicRequest updateRequest = ComicRequest.builder()
                .title("Hacked Title")
                .build();

        // Act & Assert
        assertThatThrownBy(() -> comicService.updateComic(comicId, updateRequest, null))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("You do not have permission");

        verify(comicRepository, never()).save(any(Comic.class));
    }

    // ==========================================
    // 4. DELETE COMIC & AUTHORIZATION TESTS
    // ==========================================

    @Test
    @DisplayName("Delete Comic - Thành công bởi chủ sở hữu và lên lịch dọn dẹp file")
    void deleteComic_Success_ByOwner() {
        // Arrange
        Long comicId = 1L;
        Comic comic = Comic.builder()
                .id(comicId)
                .title("Comic To Delete")
                .slug("comic-to-delete")
                .coverImage("upload/comic/comic-to-delete/cover.webp")
                .build();

        when(comicRepository.findById(comicId)).thenReturn(Optional.of(comic));
        doNothing().when(comicSecurityEvaluator).verifyOwnership(comic);

        // Act
        comicService.deleteComic(comicId);

        // Assert
        verify(comicSecurityEvaluator).verifyOwnership(comic);
        verify(fileStorageService).scheduleFileCleanupOnCommit(eq(List.of("upload/comic/comic-to-delete/cover.webp")), isNull());
        verify(fileStorageService).scheduleDirectoryCleanupOnCommit(anyString());
        verify(comicRepository).delete(comic);
    }

    @Test
    @DisplayName("Delete Comic - Ném ForbiddenException khi không có quyền xóa")
    void deleteComic_Forbidden_WhenNotOwnerOrAdmin() {
        // Arrange
        Long comicId = 1L;
        Comic comic = Comic.builder()
                .id(comicId)
                .title("Protected Comic")
                .build();

        when(comicRepository.findById(comicId)).thenReturn(Optional.of(comic));
        doThrow(new ForbiddenException("You do not have permission to perform action on this comic"))
                .when(comicSecurityEvaluator).verifyOwnership(comic);

        // Act & Assert
        assertThatThrownBy(() -> comicService.deleteComic(comicId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("You do not have permission");

        verify(comicRepository, never()).delete(any(Comic.class));
        verify(fileStorageService, never()).scheduleDirectoryCleanupOnCommit(anyString());
    }
}
