package com.comic.h.comic.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comic.h.comic.dto.request.GenreRequest;
import com.comic.h.comic.dto.response.GenreResponse;
import com.comic.h.comic.entity.Genre;

@Mapper(componentModel = "spring")
public interface GenreMapper {

    @Mapping(target = "comicCount", ignore = true)
    GenreResponse toResponse(Genre genre);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Genre toEntity(GenreRequest request);
}
