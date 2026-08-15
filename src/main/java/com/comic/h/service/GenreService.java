package com.comic.h.service;

import java.util.List;

import com.comic.h.dto.request.GenreRequest;
import com.comic.h.dto.response.GenreResponse;

public interface GenreService {

    List<GenreResponse> getAllGenres();

    GenreResponse getGenreById(Long id);

    GenreResponse getGenreBySlug(String slug);

    GenreResponse createGenre(GenreRequest request);

    GenreResponse updateGenre(Long id, GenreRequest request);

    void deleteGenre(Long id);
}
