package com.comic.h.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comic.h.dto.request.GenreRequest;
import com.comic.h.dto.response.GenreResponse;
import com.comic.h.entity.Genre;

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
