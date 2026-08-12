package com.comic.h.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.comic.h.dto.request.ComicRequest;
import com.comic.h.dto.response.ComicResponse;
import com.comic.h.service.ComicService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/comic")
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
    public ResponseEntity<List<ComicResponse>> getAllComics() {
        return ResponseEntity.ok(comicService.getAllComics());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComicResponse> getComicById(@PathVariable Long id) {
        return ResponseEntity.ok(comicService.getComicById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ComicResponse> getComicBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(comicService.getComicBySlug(slug));
    }

    @PreAuthorize("hasRole('TRANSLATOR')")
    @GetMapping("/my-comics")
    public ResponseEntity<List<ComicResponse>> getMyComics() {
        return ResponseEntity.ok(comicService.getMyComics());
    }

    @GetMapping("/uploader/{uploader}")
    public ResponseEntity<List<ComicResponse>> getComicsByUploader(@PathVariable String uploader) {
        return ResponseEntity.ok(comicService.getComicsByUploader(uploader));
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

