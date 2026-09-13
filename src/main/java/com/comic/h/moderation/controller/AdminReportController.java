package com.comic.h.moderation.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.comic.h.moderation.dto.request.ResolveReportRequest;
import com.comic.h.moderation.dto.response.ReportResponse;
import com.comic.h.moderation.enums.ReportStatus;
import com.comic.h.moderation.enums.ReportType;
import com.comic.h.moderation.service.ReportService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping
    public ResponseEntity<Page<ReportResponse>> getReports(
            @RequestParam(required = false) ReportType type,
            @RequestParam(required = false) ReportStatus status,
            @ParameterObject @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(reportService.getReports(type, status, pageable));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<ReportResponse> resolveReport(
            @PathVariable Long id,
            @Valid @RequestBody ResolveReportRequest request) {
        return ResponseEntity.ok(reportService.resolveReport(id, request));
    }
}
