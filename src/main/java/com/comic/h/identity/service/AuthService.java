package com.comic.h.identity.service;

import com.comic.h.identity.dto.request.LoginRequest;
import com.comic.h.identity.dto.request.LogoutRequest;
import com.comic.h.identity.dto.request.RefreshTokenRequest;
import com.comic.h.identity.dto.request.RegisterRequest;
import com.comic.h.identity.dto.response.AuthResponse;
import com.comic.h.identity.dto.response.RegisterResponse;

public interface AuthService {

    AuthResponse login(LoginRequest userRequest);

    RegisterResponse register(RegisterRequest userRequest);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(LogoutRequest request);
}
