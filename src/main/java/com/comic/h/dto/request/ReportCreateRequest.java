package com.comic.h.dto.request;

import com.comic.h.enums.ReportReason;
import com.comic.h.enums.ReportType;

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
    @NotNull(message = "Loại báo cáo không được để trống")
    private ReportType reportType;

    @NotNull(message = "ID mục báo cáo không được để trống")
    private Long targetId;

    @NotNull(message = "Lý do báo cáo không được để trống")
    private ReportReason reason;

    private String description;
}
