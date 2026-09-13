package com.comic.h.interaction.entity;

import com.comic.h.comic.entity.Comic;
import com.comic.h.identity.entity.User;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comic_id", nullable = false)
    private Comic comic;
}
