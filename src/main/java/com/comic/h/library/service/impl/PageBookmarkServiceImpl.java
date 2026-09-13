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
import com.comic.h.common.exception.ForbiddenException;
import com.comic.h.common.exception.ResourceNotFoundException;
import com.comic.h.identity.entity.User;
import com.comic.h.identity.service.UserService;
import com.comic.h.library.dto.request.PageBookmarkRequest;
import com.comic.h.library.dto.response.PageBookmarkResponse;
import com.comic.h.library.entity.PageBookmark;
import com.comic.h.library.mapper.PageBookmarkMapper;
import com.comic.h.library.repository.PageBookmarkRepository;
import com.comic.h.library.service.PageBookmarkService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PageBookmarkServiceImpl implements PageBookmarkService {

    private final PageBookmarkRepository pageBookmarkRepository;
    private final ComicService comicService;
    private final ChapterService chapterService;
    private final UserService userService;
    private final PageBookmarkMapper pageBookmarkMapper;

    @Override
    @Transactional
    public PageBookmarkResponse createOrUpdateBookmark(PageBookmarkRequest request, String username) {
        User user = userService.getUserEntityByUsername(username);
        Comic comic = comicService.getComicEntityById(request.getComicId());
        Chapter chapter = chapterService.getChapterEntityById(request.getChapterId());
        chapterService.validateChapterBelongsToComic(chapter, comic);

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
        User user = userService.getUserEntityByUsername(username);
        return pageBookmarkRepository.findByUserUserIdAndComicIdOrderByCreatedAtDesc(user.getUserId(), comicId)
                .stream().map(pageBookmarkMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PageBookmarkResponse> getBookmarksByChapter(Long chapterId, String username) {
        User user = userService.getUserEntityByUsername(username);
        return pageBookmarkRepository.findByUserUserIdAndChapterIdOrderByPageNumberAsc(user.getUserId(), chapterId)
                .stream().map(pageBookmarkMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PageBookmarkResponse> getUserBookmarks(String username) {
        User user = userService.getUserEntityByUsername(username);
        return pageBookmarkRepository.findAllByUserIdOrderByCreatedAtDesc(user.getUserId())
                .stream().map(pageBookmarkMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteBookmark(Long bookmarkId, String username) {
        User user = userService.getUserEntityByUsername(username);
        PageBookmark bookmark = pageBookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bookmark với id: " + bookmarkId));

        if (!bookmark.getUser().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("Bạn không có quyền xóa bookmark này");
        }

        pageBookmarkRepository.delete(bookmark);
    }
}
