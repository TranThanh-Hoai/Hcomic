package com.comic.h.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
    @DecimalMin(value = "1.0", message = "Rating score must be between 1 and 5")
    @DecimalMax(value = "5.0", message = "Rating score must be between 1 and 5")
    private Double rating;
}
