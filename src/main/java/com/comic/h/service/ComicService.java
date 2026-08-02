package com.comic.h.service;

import java.util.List;

import com.comic.h.dto.request.ComicRequest;
import com.comic.h.dto.response.ComicResponse;

public interface ComicService {

    ComicResponse createComic(ComicRequest request);

    List<ComicResponse> getAllComics();

    ComicResponse getComicById(Long id);

    ComicResponse getComicBySlug(String slug);

    ComicResponse updateComic(Long id, ComicRequest request);

    void deleteComic(Long id);
}
