package com.comic.h.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comic.h.dto.response.PageBookmarkResponse;
import com.comic.h.entity.PageBookmark;

@Mapper(componentModel = "spring")
public interface PageBookmarkMapper {

    @Mapping(source = "comic.id", target = "comicId")
    @Mapping(source = "comic.title", target = "comicTitle")
    @Mapping(source = "comic.slug", target = "comicSlug")
    @Mapping(source = "chapter.id", target = "chapterId")
    @Mapping(source = "chapter.chapterNumber", target = "chapterNumber")
    @Mapping(source = "chapter.title", target = "chapterTitle")
    @Mapping(source = "chapter.slug", target = "chapterSlug")
    PageBookmarkResponse toResponse(PageBookmark bookmark);
}
