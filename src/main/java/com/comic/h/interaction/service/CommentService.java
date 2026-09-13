package com.comic.h.interaction.service;

import org.springframework.data.domain.Pageable;

import com.comic.h.common.dto.response.PageResponse;
import com.comic.h.interaction.dto.request.CommentRequest;
import com.comic.h.interaction.dto.response.CommentResponse;

public interface CommentService {

    CommentResponse createComment(Long comicId, CommentRequest request, String username);

    CommentResponse createChapterComment(Long chapterId, CommentRequest request, String username);

    CommentResponse updateComment(Long commentId, CommentRequest request, String username);

    void deleteComment(Long commentId, String username);

    PageResponse<CommentResponse> getCommentsByComicId(Long comicId, Pageable pageable);

    PageResponse<CommentResponse> getCommentsByChapterId(Long chapterId, Pageable pageable);
}
