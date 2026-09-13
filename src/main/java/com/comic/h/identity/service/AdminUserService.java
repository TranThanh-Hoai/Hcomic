package com.comic.h.identity.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.comic.h.identity.dto.request.BanUserRequest;
import com.comic.h.identity.dto.request.UpdateRoleRequest;
import com.comic.h.identity.dto.response.AdminUserResponse;
import com.comic.h.identity.enums.Role;

public interface AdminUserService {

    Page<AdminUserResponse> searchUsers(String query, Role role, Boolean isBanned, Pageable pageable);
    
    AdminUserResponse banUser(Long userId, BanUserRequest request);
    
    AdminUserResponse unbanUser(Long userId);
    
    AdminUserResponse updateUserRole(Long userId, UpdateRoleRequest request);
}
