package com.comic.h.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.comic.h.dto.request.LibraryStatusRequest;
import com.comic.h.dto.response.UserComicLibraryResponse;
import com.comic.h.enums.ShelfStatus;
import com.comic.h.service.UserComicLibraryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class UserComicLibraryController {

    private final UserComicLibraryService userComicLibraryService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/status")
    public ResponseEntity<UserComicLibraryResponse> updateLibraryStatus(
            @Valid @RequestBody LibraryStatusRequest request,
            Authentication authentication) {
        UserComicLibraryResponse response = userComicLibraryService.updateLibraryStatus(request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<UserComicLibraryResponse>> getUserLibrary(
            @RequestParam(required = false) ShelfStatus status,
            Authentication authentication) {
        List<UserComicLibraryResponse> library = userComicLibraryService.getUserLibrary(authentication.getName(), status);
        return ResponseEntity.ok(library);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{comicId}")
    public ResponseEntity<UserComicLibraryResponse> getComicLibraryStatus(
            @PathVariable Long comicId,
            Authentication authentication) {
        UserComicLibraryResponse response = userComicLibraryService.getComicLibraryStatus(comicId, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
