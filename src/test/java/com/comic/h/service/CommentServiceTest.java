package com.comic.h.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.comic.h.dto.request.CommentRequest;
import com.comic.h.dto.response.CommentResponse;
import com.comic.h.entity.Chapter;
import com.comic.h.entity.Comic;
import com.comic.h.entity.Comment;
import com.comic.h.entity.User;
import com.comic.h.repository.ChapterRepository;
import com.comic.h.repository.ComicRepository;
import com.comic.h.repository.CommentRepository;
import com.comic.h.repository.UserRepository;
import com.comic.h.service.impl.CommentServiceImpl;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ComicRepository comicRepository;

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private UserRepository userRepository;

    private CommentServiceImpl commentService;

    private User user;
    private Comic comic;
    private Chapter chapter;

    @BeforeEach
    public void setUp() {
        commentService = new CommentServiceImpl(commentRepository, comicRepository, chapterRepository, userRepository);

        user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");
        user.setDisplayName("Test User");

        comic = Comic.builder()
                .id(100L)
                .title("Test Comic")
                .uploader("translator1")
                .build();

        chapter = Chapter.builder()
                .id(200L)
                .chapterNumber(1.0)
                .comic(comic)
                .build();
    }

    @Test
    public void testCreateComicComment() {
        CommentRequest request = CommentRequest.builder().content("Comic level comment").build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(comicRepository.findById(100L)).thenReturn(Optional.of(comic));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        CommentResponse response = commentService.createComment(100L, request, "testuser");

        assertNotNull(response);
        assertEquals(100L, response.getComicId());
        assertNull(response.getChapterId());
        assertEquals("Comic level comment", response.getContent());
    }

    @Test
    public void testCreateChapterComment() {
        CommentRequest request = CommentRequest.builder().content("Chapter level comment").build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(chapterRepository.findById(200L)).thenReturn(Optional.of(chapter));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId(2L);
            return c;
        });

        CommentResponse response = commentService.createChapterComment(200L, request, "testuser");

        assertNotNull(response);
        assertEquals(100L, response.getComicId());
        assertEquals(200L, response.getChapterId());
        assertEquals("Chapter level comment", response.getContent());
    }

    @Test
    public void testGetCommentsByComicId() {
        when(comicRepository.existsById(100L)).thenReturn(true);
        Comment comment = Comment.builder()
                .id(1L)
                .comic(comic)
                .user(user)
                .content("Comic comment")
                .build();
        when(commentRepository.findByComicIdAndChapterIsNullOrderByCreatedAtDesc(100L))
                .thenReturn(List.of(comment));

        List<CommentResponse> responses = commentService.getCommentsByComicId(100L);

        assertEquals(1, responses.size());
        assertEquals(100L, responses.get(0).getComicId());
        assertNull(responses.get(0).getChapterId());
    }

    @Test
    public void testGetCommentsByChapterId() {
        when(chapterRepository.existsById(200L)).thenReturn(true);
        Comment comment = Comment.builder()
                .id(2L)
                .comic(comic)
                .chapter(chapter)
                .user(user)
                .content("Chapter comment")
                .build();
        when(commentRepository.findByChapterIdOrderByCreatedAtDesc(200L))
                .thenReturn(List.of(comment));

        List<CommentResponse> responses = commentService.getCommentsByChapterId(200L);

        assertEquals(1, responses.size());
        assertEquals(100L, responses.get(0).getComicId());
        assertEquals(200L, responses.get(0).getChapterId());
    }
}
