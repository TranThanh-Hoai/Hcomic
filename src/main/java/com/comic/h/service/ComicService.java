package com.comic.h.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.comic.h.dto.request.ComicRequest;
import com.comic.h.dto.response.ComicResponse;

public interface ComicService {

    ComicResponse createComic(ComicRequest request, MultipartFile cover);

    List<ComicResponse> getAllComics();

    ComicResponse getComicById(Long id);

    ComicResponse getComicBySlug(String slug);

    ComicResponse updateComic(Long id, ComicRequest request, MultipartFile cover);

    void deleteComic(Long id);
}

