package com.comic.h.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.comic.h.dto.request.ChapterRequest;
import com.comic.h.dto.response.ChapterDetailResponse;
import com.comic.h.dto.response.ChapterResponse;

public interface ChapterService {

    ChapterResponse createChapter(Long comicId, ChapterRequest request, List<MultipartFile> images);

    List<ChapterResponse> getChaptersByComicSlug(String comicSlug, String sort);

    List<ChapterResponse> getChaptersByComicId(Long comicId, String sort);

    ChapterDetailResponse getChapterDetailBySlug(String comicSlug, String chapterSlug);

    ChapterResponse updateChapter(Long chapterId, ChapterRequest request, List<MultipartFile> images);

    void deleteChapter(Long chapterId);
}
