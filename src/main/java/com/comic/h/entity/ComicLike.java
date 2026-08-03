package com.comic.h.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "comic_likes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_comic_like_user_comic", columnNames = { "user_id", "comic_id" })
})
public class ComicLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "comic_id", nullable = false)
    private Comic comic;
}
