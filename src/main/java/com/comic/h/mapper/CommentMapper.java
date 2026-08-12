package com.comic.h.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comic.h.dto.response.CommentResponse;
import com.comic.h.entity.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "comic.id", target = "comicId")
    @Mapping(source = "chapter.id", target = "chapterId")
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.displayName", target = "userDisplayName")
    @Mapping(source = "user.avatar", target = "userAvatar")
    CommentResponse toResponse(Comment comment);
}
