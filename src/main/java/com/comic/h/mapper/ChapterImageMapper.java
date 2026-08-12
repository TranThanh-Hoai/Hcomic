package com.comic.h.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comic.h.dto.response.ChapterImageResponse;
import com.comic.h.entity.ChapterImage;

@Mapper(componentModel = "spring")
public interface ChapterImageMapper {

    @Mapping(source = "imagePath", target = "imageUrl")
    ChapterImageResponse toResponse(ChapterImage chapterImage);
}
