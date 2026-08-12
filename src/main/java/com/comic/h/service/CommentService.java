package com.comic.h.service;

import org.springframework.data.domain.Pageable;

import com.comic.h.dto.request.CommentRequest;
import com.comic.h.dto.response.CommentResponse;
import com.comic.h.dto.response.PageResponse;

public interface CommentService {

    CommentResponse createComment(Long comicId, CommentRequest request, String username);

    CommentResponse createChapterComment(Long chapterId, CommentRequest request, String username);

    CommentResponse updateComment(Long commentId, CommentRequest request, String username);

    void deleteComment(Long commentId, String username);

    PageResponse<CommentResponse> getCommentsByComicId(Long comicId, Pageable pageable);

    PageResponse<CommentResponse> getCommentsByChapterId(Long chapterId, Pageable pageable);
}
