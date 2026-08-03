package com.comic.h.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "comic_rate",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_comic_rate_user_comic", columnNames = {"user_id", "comic_id"})
    })
public class ComicRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "comic_id")
    private Comic comic;

    @Column(name = "rating")
    private Double rating;
}
