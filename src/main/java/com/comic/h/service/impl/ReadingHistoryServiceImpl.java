package com.comic.h.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comic.h.dto.request.ReadingHistoryRequest;
import com.comic.h.dto.response.ReadingHistoryResponse;
import com.comic.h.entity.Chapter;
import com.comic.h.entity.Comic;
import com.comic.h.entity.ReadingHistory;
import com.comic.h.entity.User;
import com.comic.h.entity.UserComicLibrary;
import com.comic.h.enums.ShelfStatus;
import com.comic.h.exception.BadRequestException;
import com.comic.h.exception.ResourceNotFoundException;
import com.comic.h.repository.ChapterRepository;
import com.comic.h.repository.ComicRepository;
import com.comic.h.repository.ReadingHistoryRepository;
import com.comic.h.repository.UserComicLibraryRepository;
import com.comic.h.repository.UserRepository;
import com.comic.h.service.ReadingHistoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReadingHistoryServiceImpl implements ReadingHistoryService {

    private final ReadingHistoryRepository readingHistoryRepository;
    private final UserComicLibraryRepository userComicLibraryRepository;
    private final ComicRepository comicRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReadingHistoryResponse saveOrUpdateProgress(ReadingHistoryRequest request, String username) {
        User user = findUserByUsername(username);
        Comic comic = comicRepository.findById(request.getComicId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy truyện với id: " + request.getComicId()));
        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chương với id: " + request.getChapterId()));
        validateChapterBelongsToComic(chapter, comic);

        Optional<ReadingHistory> existingOpt = readingHistoryRepository.findByUserUserIdAndComicId(user.getUserId(), comic.getId());

        ReadingHistory history;
        if (existingOpt.isPresent()) {
            history = existingOpt.get();
            history.setChapter(chapter);
            if (request.getPageNumber() != null) {
                history.setPageNumber(request.getPageNumber());
            }
            if (request.getPercentage() != null) {
                history.setPercentage(request.getPercentage());
            }
        } else {
            history = ReadingHistory.builder()
                    .user(user)
                    .comic(comic)
                    .chapter(chapter)
                    .pageNumber(request.getPageNumber() != null ? request.getPageNumber() : 1)
                    .percentage(request.getPercentage() != null ? request.getPercentage() : 0.0)
                    .build();
        }

        ReadingHistory saved = readingHistoryRepository.save(history);

        // Auto add to library as READING if not already in library
        boolean existsInLibrary = userComicLibraryRepository.existsByUserUserIdAndComicId(user.getUserId(), comic.getId());
        if (!existsInLibrary) {
            UserComicLibrary library = UserComicLibrary.builder()
                    .user(user)
                    .comic(comic)
                    .status(ShelfStatus.READING)
                    .build();
            userComicLibraryRepository.save(library);
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadingHistoryResponse> getUserReadingHistory(String username) {
        User user = findUserByUsername(username);
        List<ReadingHistory> histories = readingHistoryRepository.findAllByUserIdOrderByUpdatedAtDesc(user.getUserId());
        return histories.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReadingHistoryResponse getProgressByComicId(Long comicId, String username) {
        User user = findUserByUsername(username);
        return readingHistoryRepository.findByUserUserIdAndComicId(user.getUserId(), comicId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + username));
    }

    private void validateChapterBelongsToComic(Chapter chapter, Comic comic) {
        if (!chapter.getComic().getId().equals(comic.getId())) {
            throw new BadRequestException("Chapter does not belong to the requested comic");
        }
    }

    private ReadingHistoryResponse mapToResponse(ReadingHistory history) {
        return ReadingHistoryResponse.builder()
                .id(history.getId())
                .comicId(history.getComic().getId())
                .comicTitle(history.getComic().getTitle())
                .comicSlug(history.getComic().getSlug())
                .coverImage(history.getComic().getCoverImage())
                .chapterId(history.getChapter().getId())
                .chapterNumber(history.getChapter().getChapterNumber())
                .chapterTitle(history.getChapter().getTitle())
                .chapterSlug(history.getChapter().getSlug())
                .pageNumber(history.getPageNumber())
                .percentage(history.getPercentage())
                .updatedAt(history.getUpdatedAt())
                .build();
    }
}
