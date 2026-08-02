package com.comic.h.controller;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.comic.h.dto.request.ComicRequest;
import com.comic.h.dto.response.ComicResponse;
import com.comic.h.entity.ComicStatus;
import com.comic.h.service.ComicService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
class ComicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ComicService comicService;

    @Test
    @DisplayName("POST /api/comic - Tạo mới comic thành công trả về 201 CREATED")
    void createComic_Success_ShouldReturn201() throws Exception {
        ComicRequest request = ComicRequest.builder()
                .title("Solo Leveling")
                .status(ComicStatus.COMPLETED)
                .build();

        ComicResponse response = ComicResponse.builder()
                .id(1L)
                .title("Solo Leveling")
                .slug("solo-leveling")
                .status(ComicStatus.COMPLETED)
                .build();

        when(comicService.createComic(any(ComicRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/comic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Solo Leveling"))
                .andExpect(jsonPath("$.slug").value("solo-leveling"));
    }

    @Test
    @DisplayName("GET /api/comic - Lấy danh sách tất cả comics trả về 200 OK")
    void getAllComics_ShouldReturn200() throws Exception {
        ComicResponse comic1 = ComicResponse.builder().id(1L).title("Comic 1").build();
        ComicResponse comic2 = ComicResponse.builder().id(2L).title("Comic 2").build();

        when(comicService.getAllComics()).thenReturn(List.of(comic1, comic2));

        mockMvc.perform(get("/api/comic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Comic 1"))
                .andExpect(jsonPath("$[1].title").value("Comic 2"));
    }

    @Test
    @DisplayName("GET /api/comic/{id} - Lấy comic theo ID thành công trả về 200 OK")
    void getComicById_Success_ShouldReturn200() throws Exception {
        ComicResponse comic = ComicResponse.builder().id(1L).title("Comic 1").build();

        when(comicService.getComicById(1L)).thenReturn(comic);

        mockMvc.perform(get("/api/comic/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Comic 1"));
    }

    @Test
    @DisplayName("GET /api/comic/{id} - Không tìm thấy trả về 404 NOT FOUND")
    void getComicById_NotFound_ShouldThrowException() throws Exception {
        when(comicService.getComicById(99L)).thenThrow(new RuntimeException("Comic not found with id: 99"));

        mockMvc.perform(get("/api/comic/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Comic not found with id: 99"));
    }

    @Test
    @DisplayName("PUT /api/comic/{id} - Cập nhật comic thành công trả về 200 OK")
    void updateComic_Success_ShouldReturn200() throws Exception {
        ComicRequest request = ComicRequest.builder().title("Updated Title").build();
        ComicResponse response = ComicResponse.builder().id(1L).title("Updated Title").build();

        when(comicService.updateComic(eq(1L), any(ComicRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/comic/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    @DisplayName("DELETE /api/comic/{id} - Xóa comic thành công trả về 200 OK")
    void deleteComic_Success_ShouldReturn200() throws Exception {
        doNothing().when(comicService).deleteComic(1L);

        mockMvc.perform(delete("/api/comic/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Comic deleted successfully with id: 1"));
    }
}
