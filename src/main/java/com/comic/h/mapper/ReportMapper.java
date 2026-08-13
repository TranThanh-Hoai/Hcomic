package com.comic.h.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comic.h.dto.response.ReportResponse;
import com.comic.h.entity.Report;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    @Mapping(source = "report.id", target = "id")
    @Mapping(source = "report.reporter.userId", target = "reporterId")
    @Mapping(source = "report.reporter.username", target = "reporterUsername")
    @Mapping(source = "report.handledBy.username", target = "handledByUsername")
    @Mapping(source = "targetTitle", target = "targetTitle")
    ReportResponse toResponse(Report report, String targetTitle);
}
