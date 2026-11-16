package com.comic.h.comic.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comic.h.comic.dto.response.ComicResponse;
import com.comic.h.comic.entity.Comic;

@Mapper(componentModel = "spring", uses = {GenreMapper.class})
public interface ComicMapper {

    @Mapping(source = "uploader.username", target = "uploader")
    ComicResponse toResponse(Comic comic);
}
