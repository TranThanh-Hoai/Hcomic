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
public class ChapterResponse {

    private Long id;
    private Long comicId;
    private Double chapterNumber;
    private String title;
    private String slug;
    private Long viewCount;
    private Integer imageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
