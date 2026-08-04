package com.comic.h.dto.response;

import java.time.LocalDateTime;

import com.comic.h.enums.ComicStatus;

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
public class ComicResponse {

    private Long id;
    private String title;
    private String slug;
    private String description;
    private String author;
    private String uploader;
    private String coverImage;
    private Long viewCount;
    private Long likeCount;
    private Double rating;
    private ComicStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
