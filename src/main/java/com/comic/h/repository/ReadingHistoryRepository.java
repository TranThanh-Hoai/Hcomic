package com.comic.h.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.comic.h.entity.ReadingHistory;

public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, Long> {

    Optional<ReadingHistory> findByUserUserIdAndComicComicId(Long userId, Long comicId);

    @Query("SELECT rh FROM ReadingHistory rh WHERE rh.user.userId = :userId ORDER BY rh.updatedAt DESC")
    List<ReadingHistory> findAllByUserIdOrderByUpdatedAtDesc(@Param("userId") Long userId);
}
