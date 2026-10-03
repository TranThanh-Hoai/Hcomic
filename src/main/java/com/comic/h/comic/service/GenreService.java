package com.comic.h.comic.service;

import java.util.List;

import com.comic.h.comic.dto.request.GenreRequest;
import com.comic.h.comic.dto.response.GenreResponse;

public interface GenreService {

    List<GenreResponse> getAllGenres();

    GenreResponse getGenreById(Long id);

    GenreResponse getGenreBySlug(String slug);

    GenreResponse createGenre(GenreRequest request);

    GenreResponse updateGenre(Long id, GenreRequest request);

    void deleteGenre(Long id);
}
