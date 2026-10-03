package com.comic.h.identity.dto.response;

import com.comic.h.identity.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private String message;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String username;
    private Role userRole;
}
