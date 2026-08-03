package com.comic.h.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.comic.h.entity.ComicLike;

public interface ComicLikeRepository extends JpaRepository<ComicLike, Long> {

    Optional<ComicLike> findByUserUsernameAndComicId(String username, Long comicId);

    boolean existsByUserUsernameAndComicId(String username, Long comicId);

    long countByComicId(Long comicId);

    void deleteByUserUsernameAndComicId(String username, Long comicId);
}
