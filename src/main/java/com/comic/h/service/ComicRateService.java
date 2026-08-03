package com.comic.h.service;

import com.comic.h.dto.request.ComicRateRequest;
import com.comic.h.dto.response.ComicRateResponse;

public interface ComicRateService {

    double getAverageRating(Long comicId);

    ComicRateResponse rateComic(ComicRateRequest request, String username);

    ComicRateResponse getUserRatingForComic(Long comicId, String username);
}

