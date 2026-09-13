package com.comic.h.library.service;

import java.util.List;

import com.comic.h.library.dto.request.ReadingHistoryRequest;
import com.comic.h.library.dto.response.ReadingHistoryResponse;

public interface ReadingHistoryService {

    ReadingHistoryResponse saveOrUpdateProgress(ReadingHistoryRequest request, String username);

    List<ReadingHistoryResponse> getUserReadingHistory(String username);

    ReadingHistoryResponse getProgressByComicId(Long comicId, String username);
}
