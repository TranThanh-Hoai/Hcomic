package com.comic.h.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comic.h.dto.request.PageBookmarkRequest;
import com.comic.h.dto.response.PageBookmarkResponse;
import com.comic.h.entity.Chapter;
import com.comic.h.entity.Comic;
import com.comic.h.entity.PageBookmark;
import com.comic.h.entity.User;
import com.comic.h.exception.BadRequestException;
import com.comic.h.exception.ForbiddenException;
import com.comic.h.exception.ResourceNotFoundException;
import com.comic.h.mapper.PageBookmarkMapper;
import com.comic.h.repository.ChapterRepository;
import com.comic.h.repository.ComicRepository;
import com.comic.h.repository.PageBookmarkRepository;
import com.comic.h.repository.UserRepository;
import com.comic.h.service.PageBookmarkService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PageBookmarkServiceImpl implements PageBookmarkService {

    private final PageBookmarkRepository pageBookmarkRepository;
    private final ComicRepository comicRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;
    private final PageBookmarkMapper pageBookmarkMapper;

    @Override
    @Transactional
    public PageBookmarkResponse createOrUpdateBookmark(PageBookmarkRequest request, String username) {
        User user = findUserByUsername(username);
        Comic comic = comicRepository.findById(request.getComicId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy truyện với id: " + request.getComicId()));
        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chương với id: " + request.getChapterId()));
        validateChapterBelongsToComic(chapter, comic);

        Optional<PageBookmark> existingOpt = pageBookmarkRepository.findByUserUserIdAndChapterIdAndPageNumber(
                user.getUserId(), chapter.getId(), request.getPageNumber());

        PageBookmark bookmark;
        if (existingOpt.isPresent()) {
            bookmark = existingOpt.get();
            if (request.getNote() != null) {
                bookmark.setNote(request.getNote());
            }
        } else {
            bookmark = PageBookmark.builder()
                    .user(user)
                    .comic(comic)
                    .chapter(chapter)
                    .pageNumber(request.getPageNumber())
                    .note(request.getNote())
                    .build();
        }

        PageBookmark saved = pageBookmarkRepository.save(bookmark);
        return pageBookmarkMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PageBookmarkResponse> getBookmarksByComic(Long comicId, String username) {
        User user = findUserByUsername(username);
        return pageBookmarkRepository.findByUserUserIdAndComicIdOrderByCreatedAtDesc(user.getUserId(), comicId)
                .stream().map(pageBookmarkMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PageBookmarkResponse> getBookmarksByChapter(Long chapterId, String username) {
        User user = findUserByUsername(username);
        return pageBookmarkRepository.findByUserUserIdAndChapterIdOrderByPageNumberAsc(user.getUserId(), chapterId)
                .stream().map(pageBookmarkMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PageBookmarkResponse> getUserBookmarks(String username) {
        User user = findUserByUsername(username);
        return pageBookmarkRepository.findAllByUserIdOrderByCreatedAtDesc(user.getUserId())
                .stream().map(pageBookmarkMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteBookmark(Long bookmarkId, String username) {
        User user = findUserByUsername(username);
        PageBookmark bookmark = pageBookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bookmark với id: " + bookmarkId));

        if (!bookmark.getUser().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("Bạn không có quyền xóa bookmark này");
        }

        pageBookmarkRepository.delete(bookmark);
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
}
