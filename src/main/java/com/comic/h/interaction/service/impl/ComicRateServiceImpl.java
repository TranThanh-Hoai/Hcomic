package com.comic.h.interaction.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comic.h.comic.entity.Comic;
import com.comic.h.comic.service.ComicService;
import com.comic.h.common.exception.ResourceNotFoundException;
import com.comic.h.identity.entity.User;
import com.comic.h.identity.service.UserService;
import com.comic.h.interaction.dto.request.ComicRateRequest;
import com.comic.h.interaction.dto.response.ComicRateResponse;
import com.comic.h.interaction.entity.ComicRate;
import com.comic.h.interaction.mapper.ComicRateMapper;
import com.comic.h.interaction.repository.ComicRateRepository;
import com.comic.h.interaction.service.ComicRateService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComicRateServiceImpl implements ComicRateService {

    private final ComicRateRepository ratingRepository;
    private final ComicService comicService;
    private final UserService userService;
    private final ComicRateMapper comicRateMapper;

    @Override
    @Transactional(readOnly = true)
    public double getAverageRating(Long comicId) {
        Double avg = ratingRepository.getAverageRating(comicId);
        return avg != null ? avg : 0.0;
    }

    @Override
    @Transactional
    public ComicRateResponse rateComic(ComicRateRequest request, String username) {
        User user = userService.getUserEntityByUsername(username);
        Comic comic = comicService.getComicEntityById(request.getComicId());

        ComicRate rating = ratingRepository.findByUserUsernameAndComicId(username, request.getComicId())
                .orElseGet(() -> ComicRate.builder()
                        .user(user)
                        .comic(comic)
                        .build());

        rating.setRating(request.getRating());
        ComicRate savedRating = ratingRepository.save(rating);

        double avgRating = getAverageRating(comic.getId());
        comic.setAvgRating(avgRating);

        return comicRateMapper.toResponse(savedRating);
    }

    @Override
    @Transactional(readOnly = true)
    public ComicRateResponse getUserRatingForComic(Long comicId, String username) {
        ComicRate rating = ratingRepository.findByUserUsernameAndComicId(username, comicId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Rating not found for user: " + username + " on comic: " + comicId));
        return comicRateMapper.toResponse(rating);
    }

}
