package com.comic.h.interaction.service;

import com.comic.h.interaction.dto.request.ComicRateRequest;
import com.comic.h.interaction.dto.response.ComicRateResponse;

public interface ComicRateService {

    double getAverageRating(Long comicId);

    ComicRateResponse rateComic(ComicRateRequest request, String username);

    ComicRateResponse getUserRatingForComic(Long comicId, String username);
}

