package com.comic.h.service;

import java.util.List;

import com.comic.h.dto.request.CommentRequest;
import com.comic.h.dto.response.CommentResponse;

public interface CommentService {

    CommentResponse createComment(Long comicId, CommentRequest request, String username);

    CommentResponse createChapterComment(Long chapterId, CommentRequest request, String username);

    CommentResponse updateComment(Long commentId, CommentRequest request, String username);

    void deleteComment(Long commentId, String username);

    List<CommentResponse> getCommentsByComicId(Long comicId);

    List<CommentResponse> getCommentsByChapterId(Long chapterId);
}
