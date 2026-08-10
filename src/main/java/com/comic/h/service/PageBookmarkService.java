package com.comic.h.service;

import java.util.List;
import com.comic.h.dto.request.PageBookmarkRequest;
import com.comic.h.dto.response.PageBookmarkResponse;

public interface PageBookmarkService {

    PageBookmarkResponse createOrUpdateBookmark(PageBookmarkRequest request, String username);

    List<PageBookmarkResponse> getBookmarksByComic(Long comicId, String username);

    List<PageBookmarkResponse> getBookmarksByChapter(Long chapterId, String username);

    List<PageBookmarkResponse> getUserBookmarks(String username);

    void deleteBookmark(Long bookmarkId, String username);
}
