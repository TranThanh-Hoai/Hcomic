package com.comic.h.service;

import java.util.List;

import com.comic.h.dto.request.ChapterRequest;
import com.comic.h.dto.response.ChapterDetailResponse;
import com.comic.h.dto.response.ChapterResponse;

public interface ChapterService {

    ChapterResponse createChapter(Long comicId, ChapterRequest request);

    List<ChapterResponse> getChaptersByComicSlug(String comicSlug, String sort);

    List<ChapterResponse> getChaptersByComicId(Long comicId, String sort);

    ChapterResponse getChapterById(Long chapterId);

    ChapterDetailResponse getChapterDetailBySlug(String comicSlug, String chapterSlug);

    ChapterResponse updateChapter(Long chapterId, ChapterRequest request);

    void deleteChapter(Long chapterId);
}
