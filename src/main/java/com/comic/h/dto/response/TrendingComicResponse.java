package com.comic.h.dto.response;

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
public class TrendingComicResponse {
    private Long comicId;
    private String title;
    private String slug;
    private String coverImage;
    private String author;
    private long readCountInPeriod;
    private long totalViewCount;
    private double avgRating;
}
