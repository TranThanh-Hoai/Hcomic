package com.comic.h.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comic.h.dto.response.ChapterDetailResponse;
import com.comic.h.dto.response.ChapterResponse;
import com.comic.h.entity.Chapter;

@Mapper(componentModel = "spring", uses = {ChapterImageMapper.class})
public interface ChapterMapper {

    @Mapping(source = "comic.id", target = "comicId")
    @Mapping(target = "imageCount", expression = "java(chapter.getImages() != null ? chapter.getImages().size() : 0)")
    ChapterResponse toResponse(Chapter chapter);

    @Mapping(source = "chapter.id", target = "id")
    @Mapping(source = "chapter.comic.id", target = "comicId")
    @Mapping(source = "chapter.comic.title", target = "comicTitle")
    @Mapping(source = "chapter.comic.slug", target = "comicSlug")
    @Mapping(source = "chapter.chapterNumber", target = "chapterNumber")
    @Mapping(source = "chapter.title", target = "title")
    @Mapping(source = "chapter.slug", target = "slug")
    @Mapping(source = "viewCount", target = "viewCount")
    @Mapping(source = "chapter.createdAt", target = "createdAt")
    @Mapping(source = "chapter.updatedAt", target = "updatedAt")
    @Mapping(source = "chapter.images", target = "images")
    @Mapping(source = "prevChapterSlug", target = "prevChapterSlug")
    @Mapping(source = "nextChapterSlug", target = "nextChapterSlug")
    ChapterDetailResponse toDetailResponse(Chapter chapter, String prevChapterSlug, String nextChapterSlug, Long viewCount);
}
