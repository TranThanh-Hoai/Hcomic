package com.comic.h.service.impl;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ComicRepository comicRepository;

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    // ==========================================
    // 1. CREATE COMMENT TESTS
    // ==========================================

    @Test
    @DisplayName("Create Comment - Thêm comment vào Comic thành công")
    void createComment_OnComic_Success() {
        // Arrange
        Long comicId = 10L;
        String username = "reader1";
        CommentRequest request = CommentRequest.builder()
                .content("  Great comic!  ")
                .chapterId(null)
                .build();

        User user = User.builder().userId(1L).username(username).build();
        Comic comic = Comic.builder().id(comicId).title("One Piece").build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(comicRepository.findById(comicId)).thenReturn(Optional.of(comic));

        Comment savedComment = Comment.builder()
                .id(100L)
                .content("Great comic!")
                .user(user)
                .comic(comic)
                .chapter(null)
                .build();
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentResponse expectedResponse = CommentResponse.builder()
                .id(100L)
                .content("Great comic!")
                .comicId(comicId)
                .userId(1L)
                .build();
        when(commentMapper.toResponse(savedComment)).thenReturn(expectedResponse);

        // Act
        CommentResponse actualResponse = commentService.createComment(comicId, request, username);

        // Assert
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getId()).isEqualTo(100L);
        assertThat(actualResponse.getContent()).isEqualTo("Great comic!");

        verify(commentRepository).save(argThat(c ->
                c.getContent().equals("Great comic!")
                        && c.getUser().equals(user)
                        && c.getComic().equals(comic)
                        && c.getChapter() == null
        ));
    }

    @Test
    @DisplayName("Create Comment - Thêm comment vào Chapter thành công")
    void createComment_OnChapter_Success() {
        // Arrange
        Long comicId = 10L;
        Long chapterId = 55L;
        String username = "reader1";
        CommentRequest request = CommentRequest.builder()
                .content("Epic battle this chapter!")
                .chapterId(chapterId)
                .build();

        User user = User.builder().userId(1L).username(username).build();
        Comic comic = Comic.builder().id(comicId).title("One Piece").build();
        Chapter chapter = Chapter.builder().id(chapterId).comic(comic).build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(chapterRepository.findById(chapterId)).thenReturn(Optional.of(chapter));

        Comment savedComment = Comment.builder()
                .id(101L)
                .content("Epic battle this chapter!")
                .user(user)
                .comic(comic)
                .chapter(chapter)
                .build();
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentResponse expectedResponse = CommentResponse.builder()
                .id(101L)
                .content("Epic battle this chapter!")
                .chapterId(chapterId)
                .comicId(comicId)
                .build();
        when(commentMapper.toResponse(savedComment)).thenReturn(expectedResponse);

        // Act
        CommentResponse actualResponse = commentService.createComment(comicId, request, username);

        // Assert
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getContent()).isEqualTo("Epic battle this chapter!");
        assertThat(actualResponse.getChapterId()).isEqualTo(chapterId);

        verify(chapterRepository).findById(chapterId);
        verify(commentRepository).save(argThat(c ->
                c.getChapter() != null && c.getChapter().getId().equals(chapterId)
        ));
    }

    @Test
    @DisplayName("Create Comment - Ném ResourceNotFoundException khi username không tồn tại")
    void createComment_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long comicId = 10L;
        String username = "nonexistent";
        CommentRequest request = CommentRequest.builder().content("Hello").build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> commentService.createComment(comicId, request, username))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with username: nonexistent");

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Create Comment - Ném ResourceNotFoundException khi comic không tồn tại")
    void createComment_ComicNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long comicId = 999L;
        String username = "reader1";
        CommentRequest request = CommentRequest.builder().content("Hello").build();
        User user = User.builder().userId(1L).username(username).build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(comicRepository.findById(comicId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> commentService.createComment(comicId, request, username))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Comic not found with id: 999");

        verify(commentRepository, never()).save(any(Comment.class));
    }

    // ==========================================
    // 2. UPDATE COMMENT TESTS
    // ==========================================

    @Test
    @DisplayName("Update Comment - Tác giả sửa comment của mình thành công")
    void updateComment_Success_ByAuthor() {
        // Arrange
        Long commentId = 1L;
        String authorUsername = "john_doe";

        User author = User.builder().userId(5L).username(authorUsername).build();
        Comment comment = Comment.builder()
                .id(commentId)
                .content("Original comment")
                .user(author)
                .build();

        CommentRequest updateRequest = CommentRequest.builder()
                .content("  Updated comment content  ")
                .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        CommentResponse expectedResponse = CommentResponse.builder()
                .id(commentId)
                .content("Updated comment content")
                .build();
        when(commentMapper.toResponse(comment)).thenReturn(expectedResponse);

        // Act
        CommentResponse actualResponse = commentService.updateComment(commentId, updateRequest, authorUsername);

        // Assert
        assertThat(actualResponse).isNotNull();
        assertThat(comment.getContent()).isEqualTo("Updated comment content");
        verify(commentRepository).findById(commentId);
        verify(commentMapper).toResponse(comment);
    }

    @Test
    @DisplayName("Update Comment - Ném ForbiddenException khi người khác cố tình sửa comment")
    void updateComment_Forbidden_WhenNotAuthor() {
        // Arrange
        Long commentId = 1L;
        String authorUsername = "john_doe";
        String attackerUsername = "intruder";

        User author = User.builder().userId(5L).username(authorUsername).build();
        Comment comment = Comment.builder()
                .id(commentId)
                .content("Original comment")
                .user(author)
                .build();

        CommentRequest updateRequest = CommentRequest.builder()
                .content("Malicious update")
                .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // Act & Assert
        assertThatThrownBy(() -> commentService.updateComment(commentId, updateRequest, attackerUsername))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You can only edit your own comment");

        assertThat(comment.getContent()).isEqualTo("Original comment");
    }

    // ==========================================
    // 3. DELETE COMMENT TESTS
    // ==========================================

    @Test
    @DisplayName("Delete Comment - Tác giả xóa comment của mình thành công")
    void deleteComment_Success_ByAuthor() {
        // Arrange
        Long commentId = 1L;
        String authorUsername = "john_doe";

        User author = User.builder().userId(5L).username(authorUsername).build();
        Comment comment = Comment.builder()
                .id(commentId)
                .content("To be deleted")
                .user(author)
                .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // Act
        commentService.deleteComment(commentId, authorUsername);

        // Assert
        verify(commentRepository).findById(commentId);
        verify(commentRepository).delete(comment);
    }

    @Test
    @DisplayName("Delete Comment - Ném ForbiddenException khi người khác cố tình xóa comment")
    void deleteComment_Forbidden_WhenNotAuthor() {
        // Arrange
        Long commentId = 1L;
        String authorUsername = "john_doe";
        String attackerUsername = "intruder";

        User author = User.builder().userId(5L).username(authorUsername).build();
        Comment comment = Comment.builder()
                .id(commentId)
                .content("Cannot touch this")
                .user(author)
                .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // Act & Assert
        assertThatThrownBy(() -> commentService.deleteComment(commentId, attackerUsername))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You can only delete your own comment");

        verify(commentRepository, never()).delete(any(Comment.class));
    }
}
