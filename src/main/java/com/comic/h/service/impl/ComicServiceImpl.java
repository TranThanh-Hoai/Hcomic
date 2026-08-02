package com.comic.h.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comic.h.dto.request.ComicRequest;
import com.comic.h.dto.response.ComicResponse;
import com.comic.h.entity.Comic;
import com.comic.h.entity.ComicStatus;
import com.comic.h.repository.ComicRepository;
import com.comic.h.service.ComicService;
import com.comic.h.util.SlugUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComicServiceImpl implements ComicService {

    private final ComicRepository comicRepository;

    @Override
    @Transactional
    public ComicResponse createComic(ComicRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Comic title cannot be empty");
        }

        String slug = SlugUtils.toSlug(request.getTitle());

        if (comicRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        ComicStatus status = request.getStatus() != null ? request.getStatus() : ComicStatus.ONGOING;

        Comic comic = Comic.builder()
                .title(request.getTitle())
                .slug(slug)
                .status(status)
                .build();

        Comic savedComic = comicRepository.save(comic);
        return mapToResponse(savedComic);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComicResponse> getAllComics() {
        return comicRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ComicResponse getComicById(Long id) {
        Comic comic = comicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comic not found with id: " + id));
        return mapToResponse(comic);
    }

    @Override
    @Transactional(readOnly = true)
    public ComicResponse getComicBySlug(String slug) {
        Comic comic = comicRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Comic not found with slug: " + slug));
        return mapToResponse(comic);
    }

    @Override
    @Transactional
    public ComicResponse updateComic(Long id, ComicRequest request) {
        Comic comic = comicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comic not found with id: " + id));

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            comic.setTitle(request.getTitle());
            String newSlug = SlugUtils.toSlug(request.getTitle());
            if (!newSlug.equals(comic.getSlug()) && comicRepository.existsBySlug(newSlug)) {
                newSlug = newSlug + "-" + System.currentTimeMillis();
            }
            comic.setSlug(newSlug);
        }

        if (request.getStatus() != null) {
            comic.setStatus(request.getStatus());
        }

        Comic updatedComic = comicRepository.save(comic);
        return mapToResponse(updatedComic);
    }

    @Override
    @Transactional
    public void deleteComic(Long id) {
        if (!comicRepository.existsById(id)) {
            throw new RuntimeException("Comic not found with id: " + id);
        }
        comicRepository.deleteById(id);
    }

    private ComicResponse mapToResponse(Comic comic) {
        return ComicResponse.builder()
                .id(comic.getId())
                .title(comic.getTitle())
                .slug(comic.getSlug())
                .status(comic.getStatus())
                .createdAt(comic.getCreatedAt())
                .updatedAt(comic.getUpdatedAt())
                .build();
    }
}
