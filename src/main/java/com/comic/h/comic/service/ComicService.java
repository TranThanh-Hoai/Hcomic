package com.comic.h.comic.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.comic.h.comic.dto.request.ComicRequest;
import com.comic.h.comic.dto.response.ComicResponse;
import com.comic.h.comic.entity.Comic;
import com.comic.h.comic.enums.ComicStatus;
import com.comic.h.common.dto.response.PageResponse;

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

    Comic getComicEntityById(Long id);

    long countTotalComics();

    long sumTotalViewCount();

    List<Object[]> findTrendingComicsSince(LocalDateTime sinceDate, Pageable pageable);

    Page<Comic> findAllComics(Pageable pageable);
}

