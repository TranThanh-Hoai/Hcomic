package com.comic.h.dto.response;

import com.comic.h.enums.Role;

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
    private String tokenType;
    private String username;
    private Role userRole;
}
