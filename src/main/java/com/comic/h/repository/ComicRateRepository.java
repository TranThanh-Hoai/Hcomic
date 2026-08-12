package com.comic.h.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.comic.h.entity.ComicRate;

public interface ComicRateRepository extends JpaRepository<ComicRate, Long> {

    @Query("SELECT AVG(r.rating) FROM ComicRate r WHERE r.comic.id = :comicId")
    Double getAverageRating(@Param("comicId") Long comicId);

    Optional<ComicRate> findByUserUsernameAndComicId(String username, Long comicId);
}
