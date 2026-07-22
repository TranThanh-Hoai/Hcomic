package com.comic.h.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "page",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_chapter_page_num", columnNames = {"chapter_id", "page_number"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "page_id")
    private Long pageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Column(name = "image_url", nullable = false, length = 1024)
    private String imageUrl;
}
