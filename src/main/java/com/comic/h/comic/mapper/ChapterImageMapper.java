package com.comic.h.comic.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comic.h.comic.dto.response.ChapterImageResponse;
import com.comic.h.comic.entity.ChapterImage;

@Mapper(componentModel = "spring")
public interface ChapterImageMapper {

    @Mapping(source = "imagePath", target = "imageUrl")
    ChapterImageResponse toResponse(ChapterImage chapterImage);
}
