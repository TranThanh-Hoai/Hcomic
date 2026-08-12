package com.comic.h.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.comic.h.entity.PageBookmark;

public interface PageBookmarkRepository extends JpaRepository<PageBookmark, Long> {

    Optional<PageBookmark> findByUserUserIdAndChapterIdAndPageNumber(Long userId, Long chapterId, Integer pageNumber);

    List<PageBookmark> findByUserUserIdAndComicIdOrderByCreatedAtDesc(Long userId, Long comicId);

    List<PageBookmark> findByUserUserIdAndChapterIdOrderByPageNumberAsc(Long userId, Long chapterId);

    @Query("SELECT pb FROM PageBookmark pb WHERE pb.user.userId = :userId ORDER BY pb.createdAt DESC")
    List<PageBookmark> findAllByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}
