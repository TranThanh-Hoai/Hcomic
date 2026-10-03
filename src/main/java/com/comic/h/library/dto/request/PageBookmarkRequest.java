package com.comic.h.library.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageBookmarkRequest {

    @NotNull(message = "comicId must not be null")
    private Long comicId;

    @NotNull(message = "chapterId must not be null")
    private Long chapterId;

    @NotNull(message = "pageNumber must not be null")
    private Integer pageNumber;

    private String note;
}
