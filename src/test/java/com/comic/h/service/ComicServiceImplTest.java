package com.comic.h.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.comic.h.dto.request.ComicRequest;
import com.comic.h.dto.response.ComicResponse;
import com.comic.h.entity.Comic;
import com.comic.h.entity.ComicStatus;
import com.comic.h.repository.ComicRepository;
import com.comic.h.service.impl.ComicServiceImpl;

@ExtendWith(MockitoExtension.class)
class ComicServiceImplTest {

    @Mock
    private ComicRepository comicRepository;

    @InjectMocks
    private ComicServiceImpl comicService;

    @Test
    @DisplayName("createComic - Tạo mới comic thành công")
    void createComic_Success() {
        ComicRequest request = ComicRequest.builder()
                .title("One Piece")
                .status(ComicStatus.ONGOING)
                .build();

        Comic savedComic = Comic.builder()
                .id(1L)
                .title("One Piece")
                .slug("one-piece")
                .status(ComicStatus.ONGOING)
                .build();

        when(comicRepository.existsBySlug("one-piece")).thenReturn(false);
        when(comicRepository.save(any(Comic.class))).thenReturn(savedComic);

        ComicResponse response = comicService.createComic(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("One Piece", response.getTitle());
        assertEquals("one-piece", response.getSlug());
        verify(comicRepository).save(any(Comic.class));
    }

    @Test
    @DisplayName("createComic - Tiêu đề rỗng ném IllegalArgumentException")
    void createComic_EmptyTitle_ShouldThrowException() {
        ComicRequest request = ComicRequest.builder().title("").build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                comicService.createComic(request)
        );

        assertEquals("Comic title cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("getAllComics - Lấy danh sách tất cả comics")
    void getAllComics_Success() {
        Comic comic1 = Comic.builder().id(1L).title("Comic 1").slug("comic-1").build();
        Comic comic2 = Comic.builder().id(2L).title("Comic 2").slug("comic-2").build();

        when(comicRepository.findAll()).thenReturn(List.of(comic1, comic2));

        List<ComicResponse> responseList = comicService.getAllComics();

        assertEquals(2, responseList.size());
        assertEquals("Comic 1", responseList.get(0).getTitle());
        assertEquals("Comic 2", responseList.get(1).getTitle());
    }

    @Test
    @DisplayName("getComicById - Lấy thành công comic theo ID")
    void getComicById_Success() {
        Comic comic = Comic.builder().id(1L).title("Dragon Ball").slug("dragon-ball").build();

        when(comicRepository.findById(1L)).thenReturn(Optional.of(comic));

        ComicResponse response = comicService.getComicById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Dragon Ball", response.getTitle());
    }

    @Test
    @DisplayName("getComicById - Không tìm thấy ID ném RuntimeException")
    void getComicById_NotFound_ShouldThrowException() {
        when(comicRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                comicService.getComicById(99L)
        );

        assertEquals("Comic not found with id: 99", exception.getMessage());
    }

    @Test
    @DisplayName("updateComic - Cập nhật comic thành công")
    void updateComic_Success() {
        Comic existingComic = Comic.builder()
                .id(1L)
                .title("Naruto Old")
                .slug("naruto-old")
                .build();

        ComicRequest updateRequest = ComicRequest.builder()
                .title("Naruto Shippuden")
                .status(ComicStatus.COMPLETED)
                .build();

        Comic updatedComic = Comic.builder()
                .id(1L)
                .title("Naruto Shippuden")
                .slug("naruto-shippuden")
                .status(ComicStatus.COMPLETED)
                .build();

        when(comicRepository.findById(1L)).thenReturn(Optional.of(existingComic));
        when(comicRepository.existsBySlug("naruto-shippuden")).thenReturn(false);
        when(comicRepository.save(any(Comic.class))).thenReturn(updatedComic);

        ComicResponse response = comicService.updateComic(1L, updateRequest);

        assertNotNull(response);
        assertEquals("Naruto Shippuden", response.getTitle());
        assertEquals("naruto-shippuden", response.getSlug());
        assertEquals(ComicStatus.COMPLETED, response.getStatus());
    }

    @Test
    @DisplayName("deleteComic - Xóa comic thành công")
    void deleteComic_Success() {
        when(comicRepository.existsById(1L)).thenReturn(true);
        doNothing().when(comicRepository).deleteById(1L);

        comicService.deleteComic(1L);

        verify(comicRepository).deleteById(1L);
    }
}
