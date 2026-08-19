package com.comic.h.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.comic.h.entity.Chapter;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    List<Chapter> findByComicIdOrderByChapterNumberAsc(Long comicId);

    List<Chapter> findByComicIdOrderByChapterNumberDesc(Long comicId);

    List<Chapter> findByComicSlugOrderByChapterNumberAsc(String comicSlug);

    List<Chapter> findByComicSlugOrderByChapterNumberDesc(String comicSlug);

    @EntityGraph(attributePaths = {"comic", "images"})
    Optional<Chapter> findByComicSlugAndSlug(String comicSlug, String chapterSlug);

    Optional<Chapter> findByComicIdAndSlug(Long comicId, String slug);

    boolean existsByComicIdAndChapterNumber(Long comicId, Double chapterNumber);

    boolean existsByComicIdAndChapterNumberAndIdNot(Long comicId, Double chapterNumber, Long id);

    Optional<Chapter> findFirstByComicIdAndChapterNumberGreaterThanOrderByChapterNumberAsc(Long comicId, Double chapterNumber);

    Optional<Chapter> findFirstByComicIdAndChapterNumberLessThanOrderByChapterNumberDesc(Long comicId, Double chapterNumber);

    @Modifying
    @Query("UPDATE Chapter c SET c.viewCount = c.viewCount + 1 WHERE c.id = :id")
    void incrementViewCount(@Param("id") Long id);
}
