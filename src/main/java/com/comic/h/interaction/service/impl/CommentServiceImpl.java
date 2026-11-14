package com.comic.h.interaction.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comic.h.comic.entity.Chapter;
import com.comic.h.comic.entity.Comic;
import com.comic.h.comic.service.ChapterService;
import com.comic.h.comic.service.ComicService;
import com.comic.h.common.dto.response.PageResponse;
import com.comic.h.common.exception.ForbiddenException;
import com.comic.h.common.exception.ResourceNotFoundException;
import com.comic.h.identity.entity.User;
import com.comic.h.identity.service.UserService;
import com.comic.h.interaction.dto.event.CommentEvent;
import com.comic.h.interaction.dto.event.CommentEventType;
import com.comic.h.interaction.dto.request.CommentRequest;
import com.comic.h.interaction.dto.response.CommentResponse;
import com.comic.h.interaction.entity.Comment;
import com.comic.h.interaction.mapper.CommentMapper;
import com.comic.h.interaction.repository.CommentRepository;
import com.comic.h.interaction.service.CommentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ComicService comicService;
    private final ChapterService chapterService;
    private final UserService userService;
    private final CommentMapper commentMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public CommentResponse createComment(Long comicId, CommentRequest request, String username) {
        if (request.getChapterId() != null) {
            return createChapterComment(request.getChapterId(), request, username);
        }

        User user = userService.getUserEntityByUsername(username);
        Comic comic = comicService.getComicEntityById(comicId);

        Comment comment = Comment.builder()
                .content(request.getContent().trim())
                .user(user)
                .comic(comic)
                .chapter(null)
                .build();

        Comment savedComment = commentRepository.save(comment);
        CommentResponse response = commentMapper.toResponse(savedComment);
        broadcastCommentEvent(CommentEventType.CREATED, comicId, null, savedComment.getId(), response);
        return response;
    }

    @Override
    @Transactional
    public CommentResponse createChapterComment(Long chapterId, CommentRequest request, String username) {
        User user = userService.getUserEntityByUsername(username);
        Chapter chapter = chapterService.getChapterEntityById(chapterId);
        Comic comic = chapter.getComic();

        Comment comment = Comment.builder()
                .content(request.getContent().trim())
                .user(user)
                .comic(comic)
                .chapter(chapter)
                .build();

        Comment savedComment = commentRepository.save(comment);
        CommentResponse response = commentMapper.toResponse(savedComment);
        broadcastCommentEvent(CommentEventType.CREATED, comic.getId(), chapterId, savedComment.getId(), response);
        return response;
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest request, String username) {
        Comment comment = findCommentById(commentId);
        verifyOwnership(comment, username, "edit");

        comment.setContent(request.getContent().trim());
        CommentResponse response = commentMapper.toResponse(comment);
        Long comicId = comment.getComic() != null ? comment.getComic().getId() : null;
        Long chapterId = comment.getChapter() != null ? comment.getChapter().getId() : null;
        broadcastCommentEvent(CommentEventType.UPDATED, comicId, chapterId, commentId, response);
        return response;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment comment = findCommentById(commentId);
        verifyOwnership(comment, username, "delete");

        Long comicId = comment.getComic() != null ? comment.getComic().getId() : null;
        Long chapterId = comment.getChapter() != null ? comment.getChapter().getId() : null;
        commentRepository.delete(comment);
        broadcastCommentEvent(CommentEventType.DELETED, comicId, chapterId, commentId, null);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> getCommentsByComicId(Long comicId, Pageable pageable) {
        comicService.getComicEntityById(comicId);

        Page<Comment> page = commentRepository.findByComicIdAndChapterIsNull(comicId, pageable);
        Page<CommentResponse> responsePage = page.map(commentMapper::toResponse);
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> getCommentsByChapterId(Long chapterId, Pageable pageable) {
        chapterService.getChapterEntityById(chapterId);

        Page<Comment> page = commentRepository.findByChapterId(chapterId, pageable);
        Page<CommentResponse> responsePage = page.map(commentMapper::toResponse);
        return PageResponse.from(responsePage);
    }

    @Override
    public Comment getCommentEntityById(Long commentId) {
        return findCommentById(commentId);
    }

    @Override
    @Transactional
    public void deleteCommentById(Long commentId) {
        Comment comment = findCommentById(commentId);
        Long comicId = comment.getComic() != null ? comment.getComic().getId() : null;
        Long chapterId = comment.getChapter() != null ? comment.getChapter().getId() : null;
        commentRepository.delete(comment);
        broadcastCommentEvent(CommentEventType.DELETED, comicId, chapterId, commentId, null);
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

    private void broadcastCommentEvent(CommentEventType eventType, Long comicId, Long chapterId, Long commentId, CommentResponse response) {
        CommentEvent event = CommentEvent.builder()
                .eventType(eventType)
                .comicId(comicId)
                .chapterId(chapterId)
                .commentId(commentId)
                .data(response)
                .build();

        if (chapterId != null) {
            messagingTemplate.convertAndSend("/topic/chapters/" + chapterId + "/comments", event);
        } else if (comicId != null) {
            messagingTemplate.convertAndSend("/topic/comics/" + comicId + "/comments", event);
        }
    }
}
