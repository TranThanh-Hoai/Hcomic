package com.comic.h.dto.response;

import java.time.LocalDateTime;

import com.comic.h.enums.ReportReason;
import com.comic.h.enums.ReportStatus;
import com.comic.h.enums.ReportType;

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
public class ReportResponse {
    private Long id;
    private Long reporterId;
    private String reporterUsername;
    private ReportType reportType;
    private Long targetId;
    private String targetTitle;
    private ReportReason reason;
    private String description;
    private ReportStatus status;
    private String handledByUsername;
    private String resolutionNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
