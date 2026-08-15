package com.comic.h.mapper;

import org.mapstruct.Mapper;

import com.comic.h.dto.request.GenreRequest;
import com.comic.h.dto.response.GenreResponse;
import com.comic.h.entity.Genre;

@Mapper(componentModel = "spring")
public interface GenreMapper {

    GenreResponse toResponse(Genre genre);

    Genre toEntity(GenreRequest request);
}
