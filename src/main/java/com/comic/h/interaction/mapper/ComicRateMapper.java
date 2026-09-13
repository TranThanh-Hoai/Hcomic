package com.comic.h.interaction.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comic.h.interaction.dto.response.ComicRateResponse;
import com.comic.h.interaction.entity.ComicRate;

@Mapper(componentModel = "spring")
public interface ComicRateMapper {

    @Mapping(source = "comic.id", target = "comicId")
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.username", target = "username")
    ComicRateResponse toResponse(ComicRate rating);
}
