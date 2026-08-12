package com.comic.h.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.comic.h.entity.UserComicLibrary;
import com.comic.h.enums.ShelfStatus;

public interface UserComicLibraryRepository extends JpaRepository<UserComicLibrary, Long> {

    Optional<UserComicLibrary> findByUserUserIdAndComicId(Long userId, Long comicId);

    @Query("SELECT ucl FROM UserComicLibrary ucl WHERE ucl.user.userId = :userId AND (:status IS NULL OR ucl.status = :status) ORDER BY ucl.updatedAt DESC")
    List<UserComicLibrary> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") ShelfStatus status);

    boolean existsByUserUserIdAndComicId(Long userId, Long comicId);
}
