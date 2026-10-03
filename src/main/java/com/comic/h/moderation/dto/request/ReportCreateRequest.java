package com.comic.h.moderation.dto.request;

import com.comic.h.moderation.enums.ReportReason;
import com.comic.h.moderation.enums.ReportType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportCreateRequest {
    @NotNull(message = "Report type must not be null")
    private ReportType reportType;

    @NotNull(message = "Target ID must not be null")
    private Long targetId;

    @NotNull(message = "Reason must not be null")
    private ReportReason reason;

    private String description;
}
