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
import org.springframework.web.bind.annotation.RestController;

import com.comic.h.dto.request.ReadingHistoryRequest;
import com.comic.h.dto.response.ReadingHistoryResponse;
import com.comic.h.service.ReadingHistoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class ReadingHistoryController {

    private final ReadingHistoryService readingHistoryService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<ReadingHistoryResponse> saveOrUpdateProgress(
            @Valid @RequestBody ReadingHistoryRequest request,
            Authentication authentication) {
        ReadingHistoryResponse response = readingHistoryService.saveOrUpdateProgress(request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<ReadingHistoryResponse>> getUserReadingHistory(Authentication authentication) {
        List<ReadingHistoryResponse> historyList = readingHistoryService.getUserReadingHistory(authentication.getName());
        return ResponseEntity.ok(historyList);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{comicId}")
    public ResponseEntity<ReadingHistoryResponse> getProgressByComicId(
            @PathVariable Long comicId,
            Authentication authentication) {
        ReadingHistoryResponse response = readingHistoryService.getProgressByComicId(comicId, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
