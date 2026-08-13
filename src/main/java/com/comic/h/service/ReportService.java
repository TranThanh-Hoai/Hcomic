package com.comic.h.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comic.h.dto.request.BanUserRequest;
import com.comic.h.dto.request.ReportCreateRequest;
import com.comic.h.dto.request.ResolveReportRequest;
import com.comic.h.dto.response.ReportResponse;
import com.comic.h.entity.Chapter;
import com.comic.h.entity.Comic;
import com.comic.h.entity.Comment;
import com.comic.h.entity.Report;
import com.comic.h.entity.User;
import com.comic.h.enums.ReportAction;
import com.comic.h.enums.ReportStatus;
import com.comic.h.enums.ReportType;
import com.comic.h.exception.ResourceNotFoundException;
import com.comic.h.mapper.ReportMapper;
import com.comic.h.repository.ChapterRepository;
import com.comic.h.repository.ComicRepository;
import com.comic.h.repository.CommentRepository;
import com.comic.h.repository.ReportRepository;
import com.comic.h.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ChapterRepository chapterRepository;
    private final ComicRepository comicRepository;
    private final AdminUserService adminUserService;
    private final ReportMapper reportMapper;

    public ReportResponse createReport(ReportCreateRequest request) {
        User currentUser = getCurrentUser();

        Report report = Report.builder()
                .reporter(currentUser)
                .reportType(request.getReportType())
                .targetId(request.getTargetId())
                .reason(request.getReason())
                .description(request.getDescription())
                .status(ReportStatus.PENDING)
                .build();

        Report savedReport = reportRepository.save(report);
        return mapToReportResponse(savedReport);
    }

    @Transactional(readOnly = true)
    public Page<ReportResponse> getReports(ReportType reportType, ReportStatus status, Pageable pageable) {
        ReportStatus searchStatus = status != null ? status : ReportStatus.PENDING;

        Page<Report> reports;
        if (reportType != null) {
            reports = reportRepository.findByReportTypeAndStatusOrderByCreatedAtDesc(reportType, searchStatus, pageable);
        } else {
            reports = reportRepository.findByStatusOrderByCreatedAtDesc(searchStatus, pageable);
        }

        return reports.map(this::mapToReportResponse);
    }

    public ReportResponse resolveReport(Long reportId, ResolveReportRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        User adminUser = getCurrentUser();
        report.setHandledBy(adminUser);
        report.setResolutionNote(request.getResolutionNote());

        if (request.getAction() == ReportAction.DISMISS) {
            report.setStatus(ReportStatus.DISMISSED);
        } else {
            report.setStatus(ReportStatus.RESOLVED);

            if (request.getAction() == ReportAction.DELETE_CONTENT) {
                deleteTargetContent(report.getReportType(), report.getTargetId());
            } else if (request.getAction() == ReportAction.BAN_USER) {
                Long targetAuthorId = getTargetAuthorId(report.getReportType(), report.getTargetId());
                if (targetAuthorId != null) {
                    adminUserService.banUser(targetAuthorId, new BanUserRequest("Khóa tài khoản do vi phạm nội dung báo cáo #" + reportId));
                }
                deleteTargetContent(report.getReportType(), report.getTargetId());
            }
        }

        Report savedReport = reportRepository.save(report);
        return mapToReportResponse(savedReport);
    }

    private void deleteTargetContent(ReportType type, Long targetId) {
        try {
            if (type == ReportType.COMMENT) {
                commentRepository.deleteById(targetId);
            } else if (type == ReportType.CHAPTER) {
                chapterRepository.deleteById(targetId);
            } else if (type == ReportType.COMIC) {
                comicRepository.deleteById(targetId);
            }
        } catch (Exception e) {
            // Log & handle deletion error gracefully if already deleted
        }
    }

    private Long getTargetAuthorId(ReportType type, Long targetId) {
        if (type == ReportType.COMMENT) {
            return commentRepository.findById(targetId).map(c -> c.getUser().getUserId()).orElse(null);
        } else if (type == ReportType.COMIC) {
            return comicRepository.findById(targetId).map(c -> c.getUploader() != null ? c.getUploader().getUserId() : null).orElse(null);
        } else if (type == ReportType.CHAPTER) {
            return chapterRepository.findById(targetId).map(ch -> ch.getComic() != null && ch.getComic().getUploader() != null ? ch.getComic().getUploader().getUserId() : null).orElse(null);
        }
        return null;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }

    private ReportResponse mapToReportResponse(Report report) {
        String targetTitle = "Mục #" + report.getTargetId();
        try {
            if (report.getReportType() == ReportType.COMMENT) {
                Comment comment = commentRepository.findById(report.getTargetId()).orElse(null);
                if (comment != null) {
                    targetTitle = "Bình luận: \"" + (comment.getContent().length() > 30 ? comment.getContent().substring(0, 30) + "..." : comment.getContent()) + "\"";
                }
            } else if (report.getReportType() == ReportType.CHAPTER) {
                Chapter chapter = chapterRepository.findById(report.getTargetId()).orElse(null);
                if (chapter != null) {
                    targetTitle = "Chapter " + chapter.getChapterNumber() + (chapter.getComic() != null ? " (" + chapter.getComic().getTitle() + ")" : "");
                }
            } else if (report.getReportType() == ReportType.COMIC) {
                Comic comic = comicRepository.findById(report.getTargetId()).orElse(null);
                if (comic != null) {
                    targetTitle = "Truyện: " + comic.getTitle();
                }
            }
        } catch (Exception e) {
            // fallback
        }

        return reportMapper.toResponse(report, targetTitle);
    }
}
