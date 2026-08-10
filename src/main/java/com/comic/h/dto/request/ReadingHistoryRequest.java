package com.comic.h.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadingHistoryRequest {

    @NotNull(message = "comicId không được để trống")
    private Long comicId;

    @NotNull(message = "chapterId không được để trống")
    private Long chapterId;

    private Integer pageNumber;

    private Double percentage;
}
