package com.comic.h.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.comic.h.dto.request.LibraryStatusRequest;
import com.comic.h.dto.request.PageBookmarkRequest;
import com.comic.h.dto.request.ReadingHistoryRequest;
import com.comic.h.dto.response.PageBookmarkResponse;
import com.comic.h.dto.response.ReadingHistoryResponse;
import com.comic.h.dto.response.UserComicLibraryResponse;
import com.comic.h.entity.Chapter;
import com.comic.h.entity.Comic;
import com.comic.h.entity.PageBookmark;
import com.comic.h.entity.ReadingHistory;
import com.comic.h.entity.User;
import com.comic.h.entity.UserComicLibrary;
import com.comic.h.enums.ComicStatus;
import com.comic.h.enums.Role;
import com.comic.h.enums.ShelfStatus;
import com.comic.h.exception.BadRequestException;
import com.comic.h.exception.ForbiddenException;
import com.comic.h.repository.ChapterRepository;
import com.comic.h.repository.ComicRepository;
import com.comic.h.repository.PageBookmarkRepository;
import com.comic.h.repository.ReadingHistoryRepository;
import com.comic.h.repository.UserComicLibraryRepository;
import com.comic.h.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ReaderLibraryBookmarkServiceTest {

    private static final String USERNAME = "reader";

    @Mock
    private ReadingHistoryRepository readingHistoryRepository;

    @Mock
    private UserComicLibraryRepository userComicLibraryRepository;

    @Mock
    private PageBookmarkRepository pageBookmarkRepository;

    @Mock
    private ComicRepository comicRepository;

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private UserRepository userRepository;

    private User user;
    private Comic comic;
    private Chapter chapter;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setUsername(USERNAME);
        user.setPassword("secret");
        user.setRole(Role.USER);

        comic = Comic.builder()
                .id(10L)
                .title("Test Comic")
                .slug("test-comic")
                .author("Author")
                .coverImage("/cover.jpg")
                .status(ComicStatus.ONGOING)
                .build();

        chapter = Chapter.builder()
                .id(100L)
                .comic(comic)
                .chapterNumber(1.0)
                .title("Chapter 1")
                .slug("chapter-1")
                .build();
    }

    @Test
    void saveProgressCreatesReadingHistoryAndAutoAddsLibraryAsReading() {
        ReadingHistoryServiceImpl service = new ReadingHistoryServiceImpl(
                readingHistoryRepository,
                userComicLibraryRepository,
                comicRepository,
                chapterRepository,
                userRepository);
        ReadingHistoryRequest request = new ReadingHistoryRequest();
        request.setComicId(comic.getId());
        request.setChapterId(chapter.getId());
        request.setPageNumber(7);
        request.setPercentage(52.5);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(comicRepository.findById(comic.getId())).thenReturn(Optional.of(comic));
        when(chapterRepository.findById(chapter.getId())).thenReturn(Optional.of(chapter));
        when(readingHistoryRepository.findByUserUserIdAndComicId(user.getUserId(), comic.getId()))
                .thenReturn(Optional.empty());
        when(readingHistoryRepository.save(any(ReadingHistory.class))).thenAnswer(invocation -> {
            ReadingHistory history = invocation.getArgument(0);
            history.setId(500L);
            return history;
        });
        when(userComicLibraryRepository.existsByUserUserIdAndComicId(user.getUserId(), comic.getId()))
                .thenReturn(false);

        ReadingHistoryResponse response = service.saveOrUpdateProgress(request, USERNAME);

        assertThat(response.getComicId()).isEqualTo(comic.getId());
        assertThat(response.getChapterId()).isEqualTo(chapter.getId());
        assertThat(response.getPageNumber()).isEqualTo(7);
        assertThat(response.getPercentage()).isEqualTo(52.5);

        ArgumentCaptor<UserComicLibrary> libraryCaptor = ArgumentCaptor.forClass(UserComicLibrary.class);
        verify(userComicLibraryRepository).save(libraryCaptor.capture());
        assertThat(libraryCaptor.getValue().getStatus()).isEqualTo(ShelfStatus.READING);
        assertThat(libraryCaptor.getValue().getUser()).isEqualTo(user);
        assertThat(libraryCaptor.getValue().getComic()).isEqualTo(comic);
    }

    @Test
    void saveProgressUpdatesExistingHistoryWithoutDuplicatingLibrary() {
        ReadingHistoryServiceImpl service = new ReadingHistoryServiceImpl(
                readingHistoryRepository,
                userComicLibraryRepository,
                comicRepository,
                chapterRepository,
                userRepository);
        ReadingHistory existing = ReadingHistory.builder()
                .id(500L)
                .user(user)
                .comic(comic)
                .chapter(chapter)
                .pageNumber(1)
                .percentage(0.0)
                .build();
        ReadingHistoryRequest request = new ReadingHistoryRequest();
        request.setComicId(comic.getId());
        request.setChapterId(chapter.getId());
        request.setPageNumber(9);
        request.setPercentage(88.0);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(comicRepository.findById(comic.getId())).thenReturn(Optional.of(comic));
        when(chapterRepository.findById(chapter.getId())).thenReturn(Optional.of(chapter));
        when(readingHistoryRepository.findByUserUserIdAndComicId(user.getUserId(), comic.getId()))
                .thenReturn(Optional.of(existing));
        when(readingHistoryRepository.save(existing)).thenReturn(existing);
        when(userComicLibraryRepository.existsByUserUserIdAndComicId(user.getUserId(), comic.getId()))
                .thenReturn(true);

        ReadingHistoryResponse response = service.saveOrUpdateProgress(request, USERNAME);

        assertThat(response.getId()).isEqualTo(500L);
        assertThat(response.getPageNumber()).isEqualTo(9);
        assertThat(response.getPercentage()).isEqualTo(88.0);
        verify(userComicLibraryRepository, never()).save(any(UserComicLibrary.class));
    }

    @Test
    void saveProgressRejectsChapterFromAnotherComic() {
        ReadingHistoryServiceImpl service = new ReadingHistoryServiceImpl(
                readingHistoryRepository,
                userComicLibraryRepository,
                comicRepository,
                chapterRepository,
                userRepository);
        Comic anotherComic = Comic.builder().id(11L).title("Other Comic").slug("other-comic").build();
        Chapter foreignChapter = Chapter.builder()
                .id(101L)
                .comic(anotherComic)
                .chapterNumber(1.0)
                .slug("foreign")
                .build();
        ReadingHistoryRequest request = new ReadingHistoryRequest();
        request.setComicId(comic.getId());
        request.setChapterId(foreignChapter.getId());

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(comicRepository.findById(comic.getId())).thenReturn(Optional.of(comic));
        when(chapterRepository.findById(foreignChapter.getId())).thenReturn(Optional.of(foreignChapter));

        assertThatThrownBy(() -> service.saveOrUpdateProgress(request, USERNAME))
                .isInstanceOf(BadRequestException.class);
        verify(readingHistoryRepository, never()).save(any(ReadingHistory.class));
    }

    @Test
    void libraryStatusCanBeCreatedUpdatedListedAndRemoved() {
        UserComicLibraryServiceImpl service = new UserComicLibraryServiceImpl(
                userComicLibraryRepository,
                readingHistoryRepository,
                comicRepository,
                userRepository);
        UserComicLibrary existing = UserComicLibrary.builder()
                .id(700L)
                .user(user)
                .comic(comic)
                .status(ShelfStatus.READ_LATER)
                .build();
        ReadingHistory history = ReadingHistory.builder()
                .user(user)
                .comic(comic)
                .chapter(chapter)
                .pageNumber(4)
                .percentage(33.0)
                .build();
        LibraryStatusRequest updateRequest = new LibraryStatusRequest();
        updateRequest.setComicId(comic.getId());
        updateRequest.setStatus(ShelfStatus.FAVORITE);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(comicRepository.findById(comic.getId())).thenReturn(Optional.of(comic));
        when(userComicLibraryRepository.findByUserUserIdAndComicId(user.getUserId(), comic.getId()))
                .thenReturn(Optional.of(existing));
        when(userComicLibraryRepository.save(existing)).thenReturn(existing);
        when(readingHistoryRepository.findByUserUserIdAndComicId(user.getUserId(), comic.getId()))
                .thenReturn(Optional.of(history));

        UserComicLibraryResponse response = service.updateLibraryStatus(updateRequest, USERNAME);

        assertThat(response.getStatus()).isEqualTo(ShelfStatus.FAVORITE);
        assertThat(response.getLastReadChapterId()).isEqualTo(chapter.getId());
        assertThat(response.getLastReadPageNumber()).isEqualTo(4);

        when(userComicLibraryRepository.findByUserIdAndStatus(user.getUserId(), ShelfStatus.FAVORITE))
                .thenReturn(List.of(existing));
        assertThat(service.getUserLibrary(USERNAME, ShelfStatus.FAVORITE)).hasSize(1);

        LibraryStatusRequest removeRequest = new LibraryStatusRequest();
        removeRequest.setComicId(comic.getId());
        removeRequest.setStatus(null);

        assertThat(service.updateLibraryStatus(removeRequest, USERNAME)).isNull();
        verify(userComicLibraryRepository).delete(existing);
    }

    @Test
    void bookmarkCanBeCreatedUpdatedQueriedAndDeleted() {
        PageBookmarkServiceImpl service = new PageBookmarkServiceImpl(
                pageBookmarkRepository,
                comicRepository,
                chapterRepository,
                userRepository);
        PageBookmarkRequest request = new PageBookmarkRequest();
        request.setComicId(comic.getId());
        request.setChapterId(chapter.getId());
        request.setPageNumber(12);
        request.setNote("good page");

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(comicRepository.findById(comic.getId())).thenReturn(Optional.of(comic));
        when(chapterRepository.findById(chapter.getId())).thenReturn(Optional.of(chapter));
        when(pageBookmarkRepository.findByUserUserIdAndChapterIdAndPageNumber(
                user.getUserId(), chapter.getId(), request.getPageNumber())).thenReturn(Optional.empty());
        when(pageBookmarkRepository.save(any(PageBookmark.class))).thenAnswer(invocation -> {
            PageBookmark bookmark = invocation.getArgument(0);
            bookmark.setId(900L);
            return bookmark;
        });

        PageBookmarkResponse response = service.createOrUpdateBookmark(request, USERNAME);

        assertThat(response.getId()).isEqualTo(900L);
        assertThat(response.getPageNumber()).isEqualTo(12);
        assertThat(response.getNote()).isEqualTo("good page");

        PageBookmark existing = PageBookmark.builder()
                .id(900L)
                .user(user)
                .comic(comic)
                .chapter(chapter)
                .pageNumber(12)
                .note("good page")
                .build();
        request.setNote("updated note");
        when(pageBookmarkRepository.findByUserUserIdAndChapterIdAndPageNumber(
                user.getUserId(), chapter.getId(), request.getPageNumber())).thenReturn(Optional.of(existing));
        when(pageBookmarkRepository.save(existing)).thenReturn(existing);

        assertThat(service.createOrUpdateBookmark(request, USERNAME).getNote()).isEqualTo("updated note");

        when(pageBookmarkRepository.findByUserUserIdAndComicIdOrderByCreatedAtDesc(user.getUserId(), comic.getId()))
                .thenReturn(List.of(existing));
        when(pageBookmarkRepository.findByUserUserIdAndChapterIdOrderByPageNumberAsc(user.getUserId(), chapter.getId()))
                .thenReturn(List.of(existing));
        assertThat(service.getBookmarksByComic(comic.getId(), USERNAME)).hasSize(1);
        assertThat(service.getBookmarksByChapter(chapter.getId(), USERNAME)).hasSize(1);

        when(pageBookmarkRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        service.deleteBookmark(existing.getId(), USERNAME);
        verify(pageBookmarkRepository).delete(existing);
    }

    @Test
    void bookmarkRejectsChapterFromAnotherComicAndDeletionByAnotherUser() {
        PageBookmarkServiceImpl service = new PageBookmarkServiceImpl(
                pageBookmarkRepository,
                comicRepository,
                chapterRepository,
                userRepository);
        Comic anotherComic = Comic.builder().id(11L).title("Other Comic").slug("other-comic").build();
        Chapter foreignChapter = Chapter.builder()
                .id(101L)
                .comic(anotherComic)
                .chapterNumber(1.0)
                .slug("foreign")
                .build();
        PageBookmarkRequest request = new PageBookmarkRequest();
        request.setComicId(comic.getId());
        request.setChapterId(foreignChapter.getId());
        request.setPageNumber(2);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(comicRepository.findById(comic.getId())).thenReturn(Optional.of(comic));
        when(chapterRepository.findById(foreignChapter.getId())).thenReturn(Optional.of(foreignChapter));

        assertThatThrownBy(() -> service.createOrUpdateBookmark(request, USERNAME))
                .isInstanceOf(BadRequestException.class);
        verify(pageBookmarkRepository, never()).save(any(PageBookmark.class));

        User owner = new User();
        owner.setUserId(2L);
        owner.setUsername("owner");
        owner.setPassword("secret");
        owner.setRole(Role.USER);
        PageBookmark existing = PageBookmark.builder()
                .id(900L)
                .user(owner)
                .comic(comic)
                .chapter(chapter)
                .pageNumber(12)
                .build();
        when(pageBookmarkRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.deleteBookmark(existing.getId(), USERNAME))
                .isInstanceOf(ForbiddenException.class);
        verify(pageBookmarkRepository, never()).delete(existing);
    }
}
