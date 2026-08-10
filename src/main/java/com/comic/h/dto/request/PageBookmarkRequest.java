package com.comic.h.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageBookmarkRequest {

    @NotNull(message = "comicId không được để trống")
    private Long comicId;

    @NotNull(message = "chapterId không được để trống")
    private Long chapterId;

    @NotNull(message = "pageNumber không được để trống")
    private Integer pageNumber;

    private String note;
}
