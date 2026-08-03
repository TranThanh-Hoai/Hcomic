package com.comic.h.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comic.h.dto.request.CommentRequest;
import com.comic.h.dto.response.CommentResponse;
import com.comic.h.entity.Comic;
import com.comic.h.entity.Comment;
import com.comic.h.entity.User;
import com.comic.h.exception.ForbiddenException;
import com.comic.h.exception.ResourceNotFoundException;
import com.comic.h.repository.ComicRepository;
import com.comic.h.repository.CommentRepository;
import com.comic.h.repository.UserRepository;
import com.comic.h.service.CommentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ComicRepository comicRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CommentResponse createComment(Long comicId, CommentRequest request, String username) {
        User user = findUserByUsername(username);
        Comic comic = findComicById(comicId);

        Comment comment = Comment.builder()
                .content(request.getContent().trim())
                .user(user)
                .comic(comic)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return mapToResponse(savedComment);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest request, String username) {
        Comment comment = findCommentById(commentId);
        verifyOwnership(comment, username, "edit");

        comment.setContent(request.getContent().trim());
        return mapToResponse(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment comment = findCommentById(commentId);
        verifyOwnership(comment, username, "delete");

        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByComicId(Long comicId) {
        if (!comicRepository.existsById(comicId)) {
            throw new ResourceNotFoundException("Comic not found with id: " + comicId);
        }

        List<Comment> comments = commentRepository.findByComicIdOrderByCreatedAtDesc(comicId);
        return comments.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    private Comic findComicById(Long comicId) {
        return comicRepository.findById(comicId)
                .orElseThrow(() -> new ResourceNotFoundException("Comic not found with id: " + comicId));
    }

    private Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
    }

    private void verifyOwnership(Comment comment, String username, String action) {
        if (!comment.getUser().getUsername().equals(username)) {
            throw new ForbiddenException("You can only " + action + " your own comment");
        }
    }

    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .comicId(comment.getComic().getId())
                .userId(comment.getUser().getUserId())
                .userDisplayName(comment.getUser().getDisplayName())
                .userAvatar(comment.getUser().getAvatar())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
