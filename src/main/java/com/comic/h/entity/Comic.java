package com.comic.h.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.comic.h.enums.ComicStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "comics")
public class Comic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comic_id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "slug", unique = true)
    private String slug;

    @Column(name = "description")
    private String description;

    @Column(name = "author")
    private String author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User uploader;

    @Column(name = "cover_image")
    private String coverImage;


    @Builder.Default
    @Column(name = "view_count", columnDefinition = "BIGINT DEFAULT 0")
    private Long viewCount = 0L;

    @Builder.Default
    @Column(name = "like_count", columnDefinition = "BIGINT DEFAULT 0")
    private Long likeCount = 0L;

    @Builder.Default
    @Column(name = "rating", columnDefinition = "TINYINT")
    private Double avgRating = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ComicStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
