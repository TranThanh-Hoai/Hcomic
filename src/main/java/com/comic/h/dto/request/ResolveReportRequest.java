package com.comic.h.dto.request;

import com.comic.h.enums.ReportAction;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResolveReportRequest {
    @NotNull(message = "Hành động xử lý không được để trống")
    private ReportAction action;

    private String resolutionNote;
}
