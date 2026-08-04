package com.comic.h.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
public class ChapterRequest {

    @NotNull(message = "Chapter number is required")
    @Positive(message = "Chapter number must be greater than 0")
    private Double chapterNumber;

    @Size(max = 255, message = "Chapter title must not exceed 255 characters")
    private String title;
}
