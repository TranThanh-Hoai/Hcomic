package com.comic.h.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComicRateRequest {

    @NotNull(message = "Comic ID must not be null")
    private Long comicId;

    @NotNull(message = "Rating score must not be null")
    @Min(value = 1, message = "Rating score must be between 1 and 5")
    @Max(value = 5, message = "Rating score must be between 1 and 5")
    private Double rating;
}
