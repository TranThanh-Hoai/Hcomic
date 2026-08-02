package com.comic.h.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.comic.h.entity.Comic;

public interface ComicRepository extends JpaRepository<Comic, Long> {

    Optional<Comic> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByTitle(String title);
}
