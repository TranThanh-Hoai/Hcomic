package com.comic.h.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.comic.h.dto.request.ComicRequest;
import com.comic.h.dto.response.ComicResponse;
import com.comic.h.dto.response.PageResponse;
import com.comic.h.service.ComicService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping({"/api/comics"})
@RequiredArgsConstructor
public class ComicController {

    private final ComicService comicService;

    @PreAuthorize("hasAnyRole('TRANSLATOR', 'ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComicResponse> createComic(
            @Valid @RequestPart("request") ComicRequest request,
            @RequestPart("cover") MultipartFile cover) {
        ComicResponse response = comicService.createComic(request, cover);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<ComicResponse>> getAllComics(
            @RequestParam(required = false) String genre,
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(comicService.getAllComics(genre, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComicResponse> getComicById(@PathVariable Long id) {
        return ResponseEntity.ok(comicService.getComicById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ComicResponse> getComicBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(comicService.getComicBySlug(slug));
    }

    @PreAuthorize("hasAnyRole('TRANSLATOR', 'ADMIN')")
    @GetMapping("/my-comics")
    public ResponseEntity<PageResponse<ComicResponse>> getMyComics(
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(comicService.getMyComics(pageable));
    }


    @GetMapping("/uploader/{uploader}")
    public ResponseEntity<PageResponse<ComicResponse>> getComicsByUploader(
            @PathVariable String uploader,
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(comicService.getComicsByUploader(uploader, pageable));
    }

    @PreAuthorize("hasAnyRole('TRANSLATOR', 'ADMIN')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComicResponse> updateComic(
            @PathVariable Long id,
            @Valid @RequestPart("request") ComicRequest request,
            @RequestPart(value = "cover", required = false) MultipartFile cover) {
        return ResponseEntity.ok(comicService.updateComic(id, request, cover));
    }

    @PreAuthorize("hasAnyRole('TRANSLATOR', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteComic(@PathVariable Long id) {
        comicService.deleteComic(id);
        return ResponseEntity.ok("Comic deleted successfully with id: " + id);
    }
}

