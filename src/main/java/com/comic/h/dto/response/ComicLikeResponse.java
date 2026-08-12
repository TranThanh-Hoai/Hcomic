package com.comic.h.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComicLikeResponse {

    private boolean isLiked;
    private Long likeCount;
}
