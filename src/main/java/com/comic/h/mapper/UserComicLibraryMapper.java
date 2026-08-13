package com.comic.h.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comic.h.dto.response.UserComicLibraryResponse;
import com.comic.h.entity.ReadingHistory;
import com.comic.h.entity.UserComicLibrary;

@Mapper(componentModel = "spring")
public interface UserComicLibraryMapper {

    @Mapping(source = "library.id", target = "id")
    @Mapping(source = "library.comic.id", target = "comicId")
    @Mapping(source = "library.comic.title", target = "comicTitle")
    @Mapping(source = "library.comic.slug", target = "comicSlug")
    @Mapping(source = "library.comic.coverImage", target = "coverImage")
    @Mapping(source = "library.comic.author", target = "author")
    @Mapping(source = "library.comic.status", target = "comicStatus")
    @Mapping(source = "library.status", target = "status")
    @Mapping(source = "history.chapter.id", target = "lastReadChapterId")
    @Mapping(source = "history.chapter.chapterNumber", target = "lastReadChapterNumber")
    @Mapping(source = "history.chapter.slug", target = "lastReadChapterSlug")
    @Mapping(source = "history.pageNumber", target = "lastReadPageNumber")
    @Mapping(source = "history.percentage", target = "lastReadPercentage")
    @Mapping(source = "library.updatedAt", target = "updatedAt")
    UserComicLibraryResponse toResponse(UserComicLibrary library, ReadingHistory history);
}
