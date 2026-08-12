package com.comic.h.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<Comic> findByUploaderUsername(String username, Pageable pageable);

    List<Comic> findByUploader(User uploader);

    @Query("SELECT COALESCE(SUM(c.viewCount), 0) FROM Comic c")
    long sumTotalViewCount();

    @Modifying
    @Query("UPDATE Comic c SET c.viewCount = c.viewCount + 1 WHERE c.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Query("SELECT rh.comic, COUNT(rh) as readCount FROM ReadingHistory rh WHERE rh.updatedAt >= :sinceDate GROUP BY rh.comic ORDER BY readCount DESC")
    List<Object[]> findTrendingComicsSince(@Param("sinceDate") LocalDateTime sinceDate, Pageable pageable);
}
