package com.comic.h.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.comic.h.dto.request.CommentRequest;
import com.comic.h.dto.response.CommentResponse;
import com.comic.h.dto.response.PageResponse;
import com.comic.h.service.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/comics/{comicId}/comments")
    public ResponseEntity<CommentResponse> createComment(@PathVariable Long comicId,
                                           @Valid @RequestBody CommentRequest request,
                                           Authentication authentication) {
        CommentResponse response = commentService.createComment(comicId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/comics/{comicId}/comments")
    public ResponseEntity<PageResponse<CommentResponse>> getCommentsByComicId(
            @PathVariable Long comicId,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(commentService.getCommentsByComicId(comicId, pageable));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/chapters/{chapterId}/comments")
    public ResponseEntity<CommentResponse> createChapterComment(@PathVariable Long chapterId,
                                                  @Valid @RequestBody CommentRequest request,
                                                  Authentication authentication) {
        CommentResponse response = commentService.createChapterComment(chapterId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/chapters/{chapterId}/comments")
    public ResponseEntity<PageResponse<CommentResponse>> getCommentsByChapterId(
            @PathVariable Long chapterId,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(commentService.getCommentsByChapterId(chapterId, pageable));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/api/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(@PathVariable Long commentId,
                                           @Valid @RequestBody CommentRequest request,
                                           Authentication authentication) {
        CommentResponse response = commentService.updateComment(commentId, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId, Authentication authentication) {
        commentService.deleteComment(commentId, authentication.getName());
        return ResponseEntity.ok("Comment deleted successfully");
    }
}
