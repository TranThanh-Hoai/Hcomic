package com.comic.h.moderation.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.comic.h.moderation.entity.Report;
import com.comic.h.moderation.enums.ReportStatus;
import com.comic.h.moderation.enums.ReportType;

public interface ReportRepository extends JpaRepository<Report, Long> {

    @EntityGraph(attributePaths = {"reporter", "handledBy"})
    Page<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"reporter", "handledBy"})
    Page<Report> findByReportTypeAndStatusOrderByCreatedAtDesc(ReportType reportType, ReportStatus status, Pageable pageable);

    long countByStatus(ReportStatus status);

    List<Report> findByReportTypeAndTargetId(ReportType reportType, Long targetId);
}
