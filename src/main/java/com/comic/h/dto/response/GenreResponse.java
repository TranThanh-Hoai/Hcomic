package com.comic.h.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenreResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private Long comicCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
