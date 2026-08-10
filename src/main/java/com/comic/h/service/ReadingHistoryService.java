package com.comic.h.service;

import java.util.List;
import com.comic.h.dto.request.ReadingHistoryRequest;
import com.comic.h.dto.response.ReadingHistoryResponse;

public interface ReadingHistoryService {

    ReadingHistoryResponse saveOrUpdateProgress(ReadingHistoryRequest request, String username);

    List<ReadingHistoryResponse> getUserReadingHistory(String username);

    ReadingHistoryResponse getProgressByComicId(Long comicId, String username);
}
