package com.comic.h.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comic.h.dto.response.ComicRateResponse;
import com.comic.h.entity.ComicRate;

@Mapper(componentModel = "spring")
public interface ComicRateMapper {

    @Mapping(source = "comic.id", target = "comicId")
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.username", target = "username")
    ComicRateResponse toResponse(ComicRate rating);
}
