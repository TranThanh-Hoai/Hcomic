package com.comic.h.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReadingHistoryResponse {
    private Long id;
    private Long comicId;
    private String comicTitle;
    private String comicSlug;
    private String coverImage;
    private Long chapterId;
    private Double chapterNumber;
    private String chapterTitle;
    private String chapterSlug;
    private Integer pageNumber;
    private Double percentage;
    private LocalDateTime updatedAt;
}
