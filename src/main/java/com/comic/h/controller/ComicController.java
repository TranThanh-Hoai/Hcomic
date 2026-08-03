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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/comic")
@RequiredArgsConstructor
public class ComicController {

    private final ComicService comicService;

    @PreAuthorize("hasRole('TRANSLATOR')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createComic(
            @RequestPart("request") ComicRequest request,
            @RequestPart("cover") MultipartFile cover) {
        try {
            ComicResponse response = comicService.createComic(request, cover);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ComicResponse>> getAllComics() {
        return ResponseEntity.ok(comicService.getAllComics());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getComicById(@PathVariable Long id) {
        try {
            ComicResponse response = comicService.getComicById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getComicBySlug(@PathVariable String slug) {
        try {
            ComicResponse response = comicService.getComicBySlug(slug);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('TRANSLATOR')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateComic(
            @PathVariable Long id,
            @RequestPart("request") ComicRequest request,
            @RequestPart(value = "cover", required = false) MultipartFile cover) {
        try {
            ComicResponse response = comicService.updateComic(id, request, cover);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('TRANSLATOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComic(@PathVariable Long id) {
        try {
            comicService.deleteComic(id);
            return ResponseEntity.ok("Comic deleted successfully with id: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
