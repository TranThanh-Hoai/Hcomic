package com.comic.h.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.comic.h.entity.Report;
import com.comic.h.enums.ReportStatus;
import com.comic.h.enums.ReportType;

public interface ReportRepository extends JpaRepository<Report, Long> {

    Page<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);

    Page<Report> findByReportTypeAndStatusOrderByCreatedAtDesc(ReportType reportType, ReportStatus status, Pageable pageable);

    long countByStatus(ReportStatus status);

    List<Report> findByReportTypeAndTargetId(ReportType reportType, Long targetId);
}
