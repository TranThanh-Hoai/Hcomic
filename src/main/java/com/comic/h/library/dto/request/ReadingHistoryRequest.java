package com.comic.h.library.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadingHistoryRequest {

    @NotNull(message = "comicId must not be null")
    private Long comicId;

    @NotNull(message = "chapterId must not be null")
    private Long chapterId;

    private Integer pageNumber;

    private Double percentage;
}
