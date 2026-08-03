package com.comic.h.dto.response;

import java.time.LocalDateTime;
import java.util.List;

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
public class ChapterDetailResponse {

    private Long id;
    private Long comicId;
    private String comicTitle;
    private String comicSlug;
    private Double chapterNumber;
    private String title;
    private String slug;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ChapterImageResponse> images;
    private String prevChapterSlug;
    private String nextChapterSlug;
}
