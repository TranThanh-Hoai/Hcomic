package com.comic.h.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
    name = "chapters",
    indexes = {
        @Index(name = "idx_chapter_comic_number", columnList = "comic_id, chapter_number"),
        @Index(name = "idx_chapter_comic_slug", columnList = "comic_id, slug")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_chapter_comic_number", columnNames = {"comic_id", "chapter_number"}),
        @UniqueConstraint(name = "uk_chapter_comic_slug", columnNames = {"comic_id", "slug"})
    }
)
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chapter_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comic_id", nullable = false)
    private Comic comic;

    @Column(name = "chapter_number", nullable = false)
    private Double chapterNumber;

    @Column(name = "title")
    private String title;

    @Column(name = "slug", nullable = false)
    private String slug;

    @Builder.Default
    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Builder.Default
    @Column(name = "upload_status")
    private String uploadStatus = "PENDING";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ChapterImage> images = new ArrayList<>();

    public void addImage(ChapterImage image) {
        if (images == null) {
            images = new ArrayList<>();
        }
        images.add(image);
        image.setChapter(this);
    }

    public void removeImage(ChapterImage image) {
        if (images != null) {
            images.remove(image);
            image.setChapter(null);
        }
    }

    public void clearImages() {
        if (images != null) {
            for (ChapterImage image : images) {
                image.setChapter(null);
            }
            images.clear();
        }
    }
}
