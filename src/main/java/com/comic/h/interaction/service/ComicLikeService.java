package com.comic.h.interaction.service;

import com.comic.h.interaction.dto.response.ComicLikeResponse;

public interface ComicLikeService {

    ComicLikeResponse toggleLike(Long comicId, String username);

    ComicLikeResponse getLikeStatus(Long comicId, String username);
}
