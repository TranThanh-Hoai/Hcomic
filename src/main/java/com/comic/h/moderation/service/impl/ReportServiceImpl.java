package com.comic.h.moderation.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comic.h.comic.entity.Chapter;
import com.comic.h.comic.entity.Comic;
import com.comic.h.comic.service.ChapterService;
import com.comic.h.comic.service.ComicService;
import com.comic.h.common.exception.ResourceNotFoundException;
import com.comic.h.identity.dto.request.BanUserRequest;
import com.comic.h.identity.entity.User;
import com.comic.h.identity.service.AdminUserService;
import com.comic.h.identity.service.UserService;
import com.comic.h.interaction.entity.Comment;
import com.comic.h.interaction.service.CommentService;
import com.comic.h.moderation.dto.request.ReportCreateRequest;
import com.comic.h.moderation.dto.request.ResolveReportRequest;
import com.comic.h.moderation.dto.response.ReportResponse;
import com.comic.h.moderation.entity.Report;
import com.comic.h.moderation.enums.ReportAction;
import com.comic.h.moderation.enums.ReportStatus;
import com.comic.h.moderation.enums.ReportType;
import com.comic.h.moderation.mapper.ReportMapper;
import com.comic.h.moderation.repository.ReportRepository;
import com.comic.h.moderation.service.ReportService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserService userService;
    private final CommentService commentService;
    private final ChapterService chapterService;
    private final ComicService comicService;
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

    @Override
    @Transactional(readOnly = true)
    public long countPendingReports() {
        return reportRepository.countByStatus(ReportStatus.PENDING);
    }

    private void deleteTargetContent(ReportType type, Long targetId) {
        try {
            if (type == ReportType.COMMENT) {
                commentService.deleteCommentById(targetId);
            } else if (type == ReportType.CHAPTER) {
                chapterService.deleteChapter(targetId);
            } else if (type == ReportType.COMIC) {
                comicService.deleteComic(targetId);
            }
        } catch (Exception e) {
            // Log & handle deletion error gracefully if already deleted
        }
    }

    private Long getTargetAuthorId(ReportType type, Long targetId) {
        try {
            if (type == ReportType.COMMENT) {
                Comment comment = commentService.getCommentEntityById(targetId);
                return comment != null && comment.getUser() != null ? comment.getUser().getUserId() : null;
            } else if (type == ReportType.COMIC) {
                Comic comic = comicService.getComicEntityById(targetId);
                return comic != null && comic.getUploader() != null ? comic.getUploader().getUserId() : null;
            } else if (type == ReportType.CHAPTER) {
                Chapter chapter = chapterService.getChapterEntityById(targetId);
                return chapter != null && chapter.getComic() != null && chapter.getComic().getUploader() != null ? chapter.getComic().getUploader().getUserId() : null;
            }
        } catch (Exception e) {
            // fallback
        }
        return null;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        return userService.getUserEntityByUsername(auth.getName());
    }

    private ReportResponse mapToReportResponse(Report report) {
        String targetTitle = "Mục #" + report.getTargetId();
        try {
            if (report.getReportType() == ReportType.COMMENT) {
                Comment comment = commentService.getCommentEntityById(report.getTargetId());
                if (comment != null) {
                    targetTitle = "Bình luận: \"" + (comment.getContent().length() > 30 ? comment.getContent().substring(0, 30) + "..." : comment.getContent()) + "\"";
                }
            } else if (report.getReportType() == ReportType.CHAPTER) {
                Chapter chapter = chapterService.getChapterEntityById(report.getTargetId());
                if (chapter != null) {
                    targetTitle = "Chapter " + chapter.getChapterNumber() + (chapter.getComic() != null ? " (" + chapter.getComic().getTitle() + ")" : "");
                }
            } else if (report.getReportType() == ReportType.COMIC) {
                Comic comic = comicService.getComicEntityById(report.getTargetId());
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
