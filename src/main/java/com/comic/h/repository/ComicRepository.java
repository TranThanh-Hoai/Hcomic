package com.comic.h.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.comic.h.entity.Comic;
import com.comic.h.entity.User;

public interface ComicRepository extends JpaRepository<Comic, Long> {

    Optional<Comic> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByTitle(String title);

    List<Comic> findByUploaderUsernameOrderByCreatedAtDesc(String username);

    List<Comic> findByUploader(User uploader);

    @Modifying
    @Query("UPDATE Comic c SET c.viewCount = c.viewCount + 1 WHERE c.id = :id")
    void incrementViewCount(@Param("id") Long id);
}



