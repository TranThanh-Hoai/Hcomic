package com.comic.h.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comic.h.dto.request.BanUserRequest;
import com.comic.h.dto.request.UpdateRoleRequest;
import com.comic.h.dto.response.AdminUserResponse;
import com.comic.h.entity.User;
import com.comic.h.enums.Role;
import com.comic.h.exception.ResourceNotFoundException;
import com.comic.h.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> searchUsers(String query, Role role, Boolean isBanned, Pageable pageable) {
        String cleanQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        Page<User> users = userRepository.searchUsers(cleanQuery, role, isBanned, pageable);
        return users.map(this::mapToAdminUserResponse);
    }

    public AdminUserResponse banUser(Long userId, BanUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setIsBanned(true);
        user.setBanReason(request.getReason());
        user.setBannedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        return mapToAdminUserResponse(savedUser);
    }

    public AdminUserResponse unbanUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setIsBanned(false);
        user.setBanReason(null);
        user.setBannedAt(null);

        User savedUser = userRepository.save(user);
        return mapToAdminUserResponse(savedUser);
    }

    public AdminUserResponse updateUserRole(Long userId, UpdateRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setRole(request.getRole());
        User savedUser = userRepository.save(user);
        return mapToAdminUserResponse(savedUser);
    }

    private AdminUserResponse mapToAdminUserResponse(User user) {
        return AdminUserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .isBanned(user.getIsBanned() != null ? user.getIsBanned() : false)
                .banReason(user.getBanReason())
                .bannedAt(user.getBannedAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
