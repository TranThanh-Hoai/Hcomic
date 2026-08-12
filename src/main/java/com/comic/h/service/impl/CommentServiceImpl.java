package com.comic.h.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comic.h.dto.request.CommentRequest;
import com.comic.h.dto.response.CommentResponse;
import com.comic.h.entity.Chapter;
import com.comic.h.entity.Comic;
import com.comic.h.entity.Comment;
import com.comic.h.entity.User;
import com.comic.h.exception.ForbiddenException;
import com.comic.h.exception.ResourceNotFoundException;
import com.comic.h.mapper.CommentMapper;
import com.comic.h.repository.ChapterRepository;
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
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentResponse createComment(Long comicId, CommentRequest request, String username) {
        if (request.getChapterId() != null) {
            return createChapterComment(request.getChapterId(), request, username);
        }

        User user = findUserByUsername(username);
        Comic comic = findComicById(comicId);

        Comment comment = Comment.builder()
                .content(request.getContent().trim())
                .user(user)
                .comic(comic)
                .chapter(null)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toResponse(savedComment);
    }

    @Override
    @Transactional
    public CommentResponse createChapterComment(Long chapterId, CommentRequest request, String username) {
        User user = findUserByUsername(username);
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + chapterId));
        Comic comic = chapter.getComic();

        Comment comment = Comment.builder()
                .content(request.getContent().trim())
                .user(user)
                .comic(comic)
                .chapter(chapter)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toResponse(savedComment);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest request, String username) {
        Comment comment = findCommentById(commentId);
        verifyOwnership(comment, username, "edit");

        comment.setContent(request.getContent().trim());
        return commentMapper.toResponse(comment);
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

        List<Comment> comments = commentRepository.findByComicIdAndChapterIsNullOrderByCreatedAtDesc(comicId);
        return comments.stream()
                .map(commentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByChapterId(Long chapterId) {
        if (!chapterRepository.existsById(chapterId)) {
            throw new ResourceNotFoundException("Chapter not found with id: " + chapterId);
        }

        List<Comment> comments = commentRepository.findByChapterIdOrderByCreatedAtDesc(chapterId);
        return comments.stream()
                .map(commentMapper::toResponse)
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
}
