package com.comic.h.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.comic.h.dto.request.ComicRequest;
import com.comic.h.dto.response.ComicResponse;
import com.comic.h.dto.response.PageResponse;
import com.comic.h.enums.ComicStatus;

public interface ComicService {

    ComicResponse createComic(ComicRequest request, MultipartFile cover);

    PageResponse<ComicResponse> getAllComics(Pageable pageable);

    PageResponse<ComicResponse> getAllComics(String genreSlug, Pageable pageable);

    PageResponse<ComicResponse> getAllComics(
            String query,
            String genreSlug,
            List<String> genreSlugs,
            ComicStatus status,
            String uploader,
            Pageable pageable
    );

    List<ComicResponse> quickSearch(String query, int limit);

    ComicResponse getComicById(Long id);

    ComicResponse getComicBySlug(String slug);

    ComicResponse updateComic(Long id, ComicRequest request, MultipartFile cover);

    void deleteComic(Long id);

    PageResponse<ComicResponse> getMyComics(Pageable pageable);

    PageResponse<ComicResponse> getComicsByUploader(String uploader, Pageable pageable);
}
