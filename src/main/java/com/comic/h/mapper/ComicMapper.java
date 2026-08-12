package com.comic.h.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comic.h.dto.response.ComicResponse;
import com.comic.h.entity.Comic;

@Mapper(componentModel = "spring")
public interface ComicMapper {

    @Mapping(source = "uploader.username", target = "uploader")
    @Mapping(source = "avgRating", target = "rating")
    ComicResponse toResponse(Comic comic);
}
