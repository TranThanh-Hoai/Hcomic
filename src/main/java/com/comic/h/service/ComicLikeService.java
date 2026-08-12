package com.comic.h.service;

import com.comic.h.dto.response.ComicLikeResponse;

public interface ComicLikeService {

    ComicLikeResponse toggleLike(Long comicId, String username);

    ComicLikeResponse getLikeStatus(Long comicId, String username);
}
