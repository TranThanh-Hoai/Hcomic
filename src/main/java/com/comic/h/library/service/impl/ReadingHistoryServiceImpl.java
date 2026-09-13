package com.comic.h.library.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comic.h.comic.entity.Chapter;
import com.comic.h.comic.entity.Comic;
import com.comic.h.comic.service.ChapterService;
import com.comic.h.comic.service.ComicService;
import com.comic.h.identity.entity.User;
import com.comic.h.identity.service.UserService;
import com.comic.h.library.dto.request.ReadingHistoryRequest;
import com.comic.h.library.dto.response.ReadingHistoryResponse;
import com.comic.h.library.entity.ReadingHistory;
import com.comic.h.library.entity.UserComicLibrary;
import com.comic.h.library.enums.ShelfStatus;
import com.comic.h.library.mapper.ReadingHistoryMapper;
import com.comic.h.library.repository.ReadingHistoryRepository;
import com.comic.h.library.repository.UserComicLibraryRepository;
import com.comic.h.library.service.ReadingHistoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReadingHistoryServiceImpl implements ReadingHistoryService {

    private final ReadingHistoryRepository readingHistoryRepository;
    private final UserComicLibraryRepository userComicLibraryRepository;
    private final ComicService comicService;
    private final ChapterService chapterService;
    private final UserService userService;
    private final ReadingHistoryMapper readingHistoryMapper;

    @Override
    @Transactional
    public ReadingHistoryResponse saveOrUpdateProgress(ReadingHistoryRequest request, String username) {
        User user = userService.getUserEntityByUsername(username);
        Comic comic = comicService.getComicEntityById(request.getComicId());
        Chapter chapter = chapterService.getChapterEntityById(request.getChapterId());
        chapterService.validateChapterBelongsToComic(chapter, comic);

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

        return readingHistoryMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadingHistoryResponse> getUserReadingHistory(String username) {
        User user = userService.getUserEntityByUsername(username);
        List<ReadingHistory> histories = readingHistoryRepository.findAllByUserIdOrderByUpdatedAtDesc(user.getUserId());
        return histories.stream().map(readingHistoryMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReadingHistoryResponse getProgressByComicId(Long comicId, String username) {
        User user = userService.getUserEntityByUsername(username);
        return readingHistoryRepository.findByUserUserIdAndComicId(user.getUserId(), comicId)
                .map(readingHistoryMapper::toResponse)
                .orElse(null);
    }
}
