package com.comic.h.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.comic.h.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByComicIdOrderByCreatedAtDesc(Long comicId);

    List<Comment> findByComicIdAndChapterIsNullOrderByCreatedAtDesc(Long comicId);

    List<Comment> findByChapterIdOrderByCreatedAtDesc(Long chapterId);
}
