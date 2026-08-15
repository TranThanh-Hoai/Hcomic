package com.comic.h.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.comic.h.entity.Genre;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    Optional<Genre> findBySlug(String slug);

    Optional<Genre> findByName(String name);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    List<Genre> findAllByOrderByNameAsc();

    @Query("SELECT g, COUNT(c) FROM Genre g LEFT JOIN Comic c ON g MEMBER OF c.genres GROUP BY g ORDER BY g.name ASC")
    List<Object[]> findAllGenresWithComicCount();

    @Query("SELECT COUNT(c) FROM Comic c JOIN c.genres g WHERE g.id = :genreId")
    long countComicsByGenreId(@Param("genreId") Long genreId);
}
