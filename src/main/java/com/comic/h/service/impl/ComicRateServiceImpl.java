package com.comic.h.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comic.h.dto.request.ComicRateRequest;
import com.comic.h.dto.response.ComicRateResponse;
import com.comic.h.entity.Comic;
import com.comic.h.entity.ComicRate;
import com.comic.h.entity.User;
import com.comic.h.repository.ComicRepository;
import com.comic.h.repository.ComicRateRepository;
import com.comic.h.repository.UserRepository;
import com.comic.h.service.ComicRateService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComicRateServiceImpl implements ComicRateService {

    private final ComicRateRepository ratingRepository;
    private final ComicRepository comicRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public double getAverageRating(Long comicId) {
        Double avg = ratingRepository.getAverageRating(comicId);
        return avg != null ? avg : 0.0;
    }

    @Override
    @Transactional
    public ComicRateResponse rateComic(ComicRateRequest request, String username) {
        if (request == null || request.getComicId() == null) {
            throw new IllegalArgumentException("Comic ID must not be null");
        }
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating score must be between 1 and 5");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        Comic comic = comicRepository.findById(request.getComicId())
                .orElseThrow(() -> new RuntimeException("Comic not found with id: " + request.getComicId()));

        ComicRate rating = ratingRepository.findByUserUsernameAndComicId(username, request.getComicId())
                .orElseGet(() -> ComicRate.builder()
                        .user(user)
                        .comic(comic)
                        .build());

        rating.setRating(request.getRating());
        ComicRate savedRating = ratingRepository.save(rating);

        double avgRating = getAverageRating(comic.getId());
        comic.setAvgRating(avgRating);
        comicRepository.save(comic);

        return mapToResponse(savedRating);
    }

    @Override
    @Transactional(readOnly = true)
    public ComicRateResponse getUserRatingForComic(Long comicId, String username) {
        ComicRate rating = ratingRepository.findByUserUsernameAndComicId(username, comicId)
                .orElseThrow(
                        () -> new RuntimeException("Rating not found for user: " + username + " on comic: " + comicId));
        return mapToResponse(rating);
    }

    private ComicRateResponse mapToResponse(ComicRate rating) {
        return ComicRateResponse.builder()
                .id(rating.getId())
                .comicId(rating.getComic().getId())
                .userId(rating.getUser().getUserId())
                .username(rating.getUser().getUsername())
                .rating(rating.getRating())
                .build();
    }
}
