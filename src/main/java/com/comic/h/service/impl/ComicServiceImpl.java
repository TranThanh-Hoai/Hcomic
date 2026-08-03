package com.comic.h.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.comic.h.dto.request.ComicRequest;
import com.comic.h.dto.response.ComicResponse;
import com.comic.h.entity.Comic;
import com.comic.h.enums.ComicStatus;
import com.comic.h.repository.ComicRepository;
import com.comic.h.service.ComicService;
import com.comic.h.util.SlugUtils;
import com.comic.h.util.UploadUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComicServiceImpl implements ComicService {

    @Value("${app.upload.comic-dir}")
    private String UPLOAD_DIR;

    private final ComicRepository comicRepository;

    @Override
    @Transactional
    public ComicResponse createComic(ComicRequest request, MultipartFile cover) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Comic title cannot be empty");
        }

        String slug = SlugUtils.toSlug(request.getTitle());

        if (comicRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        ComicStatus status = request.getStatus() != null ? request.getStatus() : ComicStatus.ONGOING;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String uploader = authentication.getName();

        String coverImagePath = null;
        if (cover != null && !cover.isEmpty()) {
            try {
                coverImagePath = UploadUtils.saveFile(cover, UPLOAD_DIR);
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload comic cover image: " + e.getMessage(), e);
            }
        }

        Comic comic = Comic.builder()
                .title(request.getTitle())
                .slug(slug)
                .description(request.getDescription())
                .author(request.getAuthor())
                .uploader(uploader)
                .coverImage(coverImagePath)
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
    public ComicResponse updateComic(Long id, ComicRequest request, MultipartFile cover) {
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

        if (request.getDescription() != null) {
            comic.setDescription(request.getDescription());
        }

        if (request.getAuthor() != null) {
            comic.setAuthor(request.getAuthor());
        }

        if (request.getStatus() != null) {
            comic.setStatus(request.getStatus());
        }

        if (cover != null && !cover.isEmpty()) {
            try {
                String savedPath = UploadUtils.saveFile(cover, UPLOAD_DIR);
                comic.setCoverImage(savedPath);
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload comic cover image: " + e.getMessage(), e);
            }
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
                .description(comic.getDescription())
                .author(comic.getAuthor())
                .uploader(comic.getUploader())
                .coverImage(comic.getCoverImage())
                .viewCount(comic.getViewCount())
                .likeCount(comic.getLikeCount())
                .rating(comic.getAvgRating())
                .status(comic.getStatus())
                .createdAt(comic.getCreatedAt())
                .updatedAt(comic.getUpdatedAt())
                .build();
    }

    @Transactional
    public long increaseView(long id) {
        Comic comic = comicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comic not found with id: " + id));
        comic.setViewCount(comic.getViewCount() + 1);
        comicRepository.save(comic);
        return comic.getViewCount();
    }
}
