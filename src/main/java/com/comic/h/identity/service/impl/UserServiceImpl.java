package com.comic.h.identity.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comic.h.common.exception.ResourceNotFoundException;
import com.comic.h.identity.entity.User;
import com.comic.h.identity.repository.UserRepository;
import com.comic.h.identity.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getUserEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + username));
    }

    @Override
    public User getUserEntityById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với id: " + userId));
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public long countTotalUsers() {
        return userRepository.count();
    }

    @Override
    public long countNewUsersAfter(LocalDateTime dateTime) {
        return userRepository.countByCreatedAtAfter(dateTime);
    }

    @Override
    public long countBannedUsers() {
        return userRepository.countByIsBannedTrue();
    }

    @Override
    public List<Object[]> countUsersGroupedByDate(LocalDateTime startDateTime) {
        return userRepository.countUsersGroupedByDate(startDateTime);
    }
}
