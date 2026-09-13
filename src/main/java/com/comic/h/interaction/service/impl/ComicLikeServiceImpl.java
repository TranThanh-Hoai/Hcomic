package com.comic.h.interaction.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comic.h.comic.entity.Comic;
import com.comic.h.comic.service.ComicService;
import com.comic.h.identity.entity.User;
import com.comic.h.identity.service.UserService;
import com.comic.h.interaction.dto.response.ComicLikeResponse;
import com.comic.h.interaction.entity.ComicLike;
import com.comic.h.interaction.repository.ComicLikeRepository;
import com.comic.h.interaction.service.ComicLikeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComicLikeServiceImpl implements ComicLikeService {

    private final ComicLikeRepository comicLikeRepository;
    private final ComicService comicService;
    private final UserService userService;

    @Override
    @Transactional
    public ComicLikeResponse toggleLike(Long comicId, String username) {
        User user = findUserByUsername(username);
        Comic comic = findComicById(comicId);

        boolean liked = comicLikeRepository.existsByUserUsernameAndComicId(username, comicId);

        if (liked) {
            comicLikeRepository.deleteByUserUsernameAndComicId(username, comicId);
            liked = false;
        } else {
            ComicLike comicLike = ComicLike.builder()
                    .user(user)
                    .comic(comic)
                    .build();
            comicLikeRepository.save(comicLike);
            liked = true;
        }

        long likeCount = updateComicLikeCount(comic);

        return buildResponse(liked, likeCount);
    }

    @Override
    @Transactional(readOnly = true)
    public ComicLikeResponse getLikeStatus(Long comicId, String username) {
        boolean isLiked = comicLikeRepository.existsByUserUsernameAndComicId(username, comicId);
        long likeCount = comicLikeRepository.countByComicId(comicId);
        return buildResponse(isLiked, likeCount);
    }

    private long updateComicLikeCount(Comic comic) {
        long count = comicLikeRepository.countByComicId(comic.getId());
        comic.setLikeCount(count);
        return count;
    }

    private User findUserByUsername(String username) {
        return userService.getUserEntityByUsername(username);
    }

    private Comic findComicById(Long comicId) {
        return comicService.getComicEntityById(comicId);
    }

    private ComicLikeResponse buildResponse(boolean isLiked, long likeCount) {
        return ComicLikeResponse.builder()
                .isLiked(isLiked)
                .likeCount(likeCount)
                .build();
    }
}
