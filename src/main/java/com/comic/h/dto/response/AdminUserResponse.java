package com.comic.h.dto.response;

import java.time.LocalDateTime;

import com.comic.h.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserResponse {
    private Long userId;
    private String username;
    private String email;
    private String displayName;
    private String avatar;
    private Role role;
    private Boolean isBanned;
    private String banReason;
    private LocalDateTime bannedAt;
    private LocalDateTime createdAt;
}
