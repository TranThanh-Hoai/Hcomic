package com.comic.h.identity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.comic.h.identity.dto.response.AdminUserResponse;
import com.comic.h.identity.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "isBanned", expression = "java(user.getIsBanned() != null ? user.getIsBanned() : false)")
    AdminUserResponse toAdminUserResponse(User user);
}
