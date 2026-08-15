package com.comic.h.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.comic.h.dto.request.ComicRequest;
import com.comic.h.dto.response.ComicResponse;
import com.comic.h.dto.response.PageResponse;
import com.comic.h.entity.Comic;
import com.comic.h.entity.Genre;
import com.comic.h.entity.User;
import com.comic.h.enums.ComicStatus;
import com.comic.h.exception.ForbiddenException;
import com.comic.h.exception.ResourceNotFoundException;
import com.comic.h.mapper.ComicMapper;
import com.comic.h.repository.ChapterImageRepository;
import com.comic.h.repository.ComicRepository;
import com.comic.h.repository.GenreRepository;
import com.comic.h.repository.UserRepository;
import com.comic.h.security.ComicSecurityEvaluator;
import com.comic.h.service.ComicService;
import com.comic.h.service.FileStorageService;
import com.comic.h.util.ImageProcessor;
import com.comic.h.util.SlugUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComicServiceImpl implements ComicService {

    @Value("${app.upload.comic-dir:upload/comic}")
    private String uploadDir = "upload/comic";

    private final ComicRepository comicRepository;
    private final GenreRepository genreRepository;
    private final ChapterImageRepository chapterImageRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final ImageProcessor imageProcessor;
    private final ComicSecurityEvaluator comicSecurityEvaluator;
    private final ComicMapper comicMapper;

    @Override
    @Transactional
    public ComicResponse createComic(ComicRequest request, MultipartFile cover) {
        String slug = SlugUtils.toSlug(request.getTitle());

        if (comicRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        ComicStatus status = request.getStatus() != null ? request.getStatus() : ComicStatus.ONGOING;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User uploader = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        String coverImagePath = saveCoverImage(cover, slug);

        Set<Genre> genres = new HashSet<>();
        if (request.getGenreIds() != null && !request.getGenreIds().isEmpty()) {
            genres = new HashSet<>(genreRepository.findAllById(request.getGenreIds()));
        }

        Comic comic = Comic.builder()
                .title(request.getTitle())
                .slug(slug)
                .description(request.getDescription())
                .author(request.getAuthor())
                .uploader(uploader)
                .coverImage(coverImagePath)
                .status(status)
                .genres(genres)
                .build();

        Comic savedComic = comicRepository.save(comic);
        return comicMapper.toResponse(savedComic);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ComicResponse> getAllComics(Pageable pageable) {
        return getAllComics(null, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ComicResponse> getAllComics(String genreSlug, Pageable pageable) {
        Page<Comic> page;
        if (genreSlug != null && !genreSlug.trim().isEmpty() && !"ALL".equalsIgnoreCase(genreSlug.trim())) {
            page = comicRepository.findByGenreSlug(genreSlug.trim(), pageable);
        } else {
            page = comicRepository.findAll(pageable);
        }
        Page<ComicResponse> responsePage = page.map(comicMapper::toResponse);
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public ComicResponse getComicById(Long id) {
        Comic comic = comicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comic not found with id: " + id));
        return comicMapper.toResponse(comic);
    }

    @Override
    @Transactional(readOnly = true)
    public ComicResponse getComicBySlug(String slug) {
        Comic comic = comicRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Comic not found with slug: " + slug));
        return comicMapper.toResponse(comic);
    }

    @Override
    @Transactional
    public ComicResponse updateComic(Long id, ComicRequest request, MultipartFile cover) {
        Comic comic = comicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comic not found with id: " + id));

        comicSecurityEvaluator.verifyOwnership(comic);

        String oldSlug = comic.getSlug();
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            comic.setTitle(request.getTitle());

            String newSlug = SlugUtils.toSlug(request.getTitle());

            if (!newSlug.equals(comic.getSlug()) && comicRepository.existsBySlug(newSlug)) {
                newSlug = newSlug + "-" + System.currentTimeMillis();
            }

            if (!newSlug.equals(oldSlug)) {
                Path oldDir = Paths.get(uploadDir, oldSlug);
                Path newDir = Paths.get(uploadDir, newSlug);
                boolean moved = fileStorageService.moveDirectory(oldDir.toString(), newDir.toString());
                if (moved) {
                    if (comic.getCoverImage() != null) {
                        comic.setCoverImage(comic.getCoverImage().replace(oldSlug, newSlug));
                    }
                    chapterImageRepository.updateImagePathsForComicSlugChange(comic.getId(), "/" + oldSlug + "/", "/" + newSlug + "/");
                    chapterImageRepository.updateImagePathsForComicSlugChange(comic.getId(), oldSlug + "/", newSlug + "/");
                }
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

        if (request.getGenreIds() != null) {
            Set<Genre> genres = new HashSet<>(genreRepository.findAllById(request.getGenreIds()));
            comic.setGenres(genres);
        }

        String newCoverPath = null;
        if (cover != null && !cover.isEmpty()) {
            String oldCoverPath = comic.getCoverImage();
            newCoverPath = saveCoverImage(cover, comic.getSlug());
            if (newCoverPath != null) {
                List<String> filesToDeleteOnCommit = (oldCoverPath != null && !oldCoverPath.equalsIgnoreCase(newCoverPath))
                        ? List.of(oldCoverPath)
                        : null;
                List<String> filesToDeleteOnRollback = List.of(newCoverPath);
                fileStorageService.scheduleFileCleanupOnCommit(filesToDeleteOnCommit, filesToDeleteOnRollback);
                comic.setCoverImage(newCoverPath);
            }
        }

        try {
            Comic savedComic = comicRepository.save(comic);
            return comicMapper.toResponse(savedComic);
        } catch (RuntimeException e) {
            if (!TransactionSynchronizationManager.isActualTransactionActive() && newCoverPath != null) {
                fileStorageService.deleteFile(newCoverPath);
            }
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteComic(Long id) {
        Comic comic = comicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comic not found with id: " + id));

        comicSecurityEvaluator.verifyOwnership(comic);

        if (comic.getCoverImage() != null) {
            fileStorageService.scheduleFileCleanupOnCommit(List.of(comic.getCoverImage()), null);
        }

        Path comicDir = Paths.get(uploadDir, comic.getSlug());
        fileStorageService.scheduleDirectoryCleanupOnCommit(comicDir.toString());

        comicRepository.delete(comic);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ComicResponse> getMyComics(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("User is not authenticated");
        }
        String currentUsername = authentication.getName();
        return getComicsByUploader(currentUsername, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ComicResponse> getComicsByUploader(String uploader, Pageable pageable) {
        Page<Comic> page = comicRepository.findByUploaderUsername(uploader, pageable);
        Page<ComicResponse> responsePage = page.map(comicMapper::toResponse);
        return PageResponse.from(responsePage);
    }

    @Transactional
    public long increaseView(long id) {
        if (!comicRepository.existsById(id)) {
            throw new ResourceNotFoundException("Comic not found with id: " + id);
        }
        comicRepository.incrementViewCount(id);
        Comic comic = comicRepository.findById(id).orElseThrow();
        return comic.getViewCount() != null ? comic.getViewCount() : 0L;
    }

    private String saveCoverImage(MultipartFile cover, String slug) {
        if (cover == null || cover.isEmpty()) {
            return null;
        }
        try {
            Path comicDir = Paths.get(uploadDir, slug);
            String coverFileName = slug + "-cover.webp";
            byte[] webpBytes = imageProcessor.convertToWebp(cover);
            return fileStorageService.saveFile(webpBytes, comicDir.toString(), coverFileName).replace('\\', '/');
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload comic cover image: " + e.getMessage(), e);
        }
    }
}
