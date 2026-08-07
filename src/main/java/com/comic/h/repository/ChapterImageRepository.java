package com.comic.h.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.comic.h.entity.ChapterImage;

public interface ChapterImageRepository extends JpaRepository<ChapterImage, Long> {

    Optional<ChapterImage> findByChapterIdAndPageNumber(Long chapterId, Integer pageNumber);

    List<ChapterImage> findByChapterIdOrderByPageNumberAsc(Long chapterId);

    void deleteByChapterIdAndPageNumber(Long chapterId, Integer pageNumber);

    @Modifying
    @Query("DELETE FROM ChapterImage ci WHERE ci.chapter.id = :chapterId")
    long deleteByChapterId(@Param("chapterId") Long chapterId);

    @Modifying
    @Query("UPDATE ChapterImage ci SET ci.imagePath = REPLACE(ci.imagePath, :oldPrefix, :newPrefix) WHERE ci.chapter.id IN (SELECT c.id FROM Chapter c WHERE c.comic.id = :comicId)")
    int updateImagePathsForComicSlugChange(@Param("comicId") Long comicId, @Param("oldPrefix") String oldPrefix, @Param("newPrefix") String newPrefix);
}
