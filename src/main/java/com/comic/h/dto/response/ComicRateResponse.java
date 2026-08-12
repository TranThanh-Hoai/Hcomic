package com.comic.h.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComicRateResponse {

    private Long id;
    private Long comicId;
    private Long userId;
    private String username;
    private Double rating;
}
