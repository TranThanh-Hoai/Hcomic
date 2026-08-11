package com.comic.h.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.comic.h.dto.request.BanUserRequest;
import com.comic.h.dto.request.UpdateRoleRequest;
import com.comic.h.dto.response.AdminUserResponse;
import com.comic.h.enums.Role;
import com.comic.h.service.AdminUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<Page<AdminUserResponse>> searchUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean isBanned,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminUserService.searchUsers(query, role, isBanned, pageable));
    }

    @PutMapping("/{userId}/ban")
    public ResponseEntity<AdminUserResponse> banUser(
            @PathVariable Long userId,
            @Valid @RequestBody BanUserRequest request) {
        return ResponseEntity.ok(adminUserService.banUser(userId, request));
    }

    @PutMapping("/{userId}/unban")
    public ResponseEntity<AdminUserResponse> unbanUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminUserService.unbanUser(userId));
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<AdminUserResponse> updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(adminUserService.updateUserRole(userId, request));
    }
}
