package com.comic.h.identity.service;

import java.time.LocalDateTime;
import java.util.List;

import com.comic.h.identity.entity.User;

public interface UserService {

    User getUserEntityByUsername(String username);

    User getUserEntityById(Long userId);

    boolean existsByUsername(String username);

    long countTotalUsers();

    long countNewUsersAfter(LocalDateTime dateTime);

    long countBannedUsers();

    List<Object[]> countUsersGroupedByDate(LocalDateTime startDateTime);
}
