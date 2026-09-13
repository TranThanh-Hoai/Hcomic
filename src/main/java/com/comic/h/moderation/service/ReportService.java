package com.comic.h.moderation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.comic.h.moderation.dto.request.ReportCreateRequest;
import com.comic.h.moderation.dto.request.ResolveReportRequest;
import com.comic.h.moderation.dto.response.ReportResponse;
import com.comic.h.moderation.enums.ReportStatus;
import com.comic.h.moderation.enums.ReportType;

@Transactional
public interface ReportService {

    ReportResponse createReport(ReportCreateRequest request);

    Page<ReportResponse> getReports(ReportType reportType, ReportStatus status, Pageable pageable);

    ReportResponse resolveReport(Long reportId, ResolveReportRequest request);

    long countPendingReports();
}