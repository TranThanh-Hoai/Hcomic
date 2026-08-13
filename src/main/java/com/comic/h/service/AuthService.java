package com.comic.h.service;

import com.comic.h.dto.request.LoginRequest;
import com.comic.h.dto.request.LogoutRequest;
import com.comic.h.dto.request.RefreshTokenRequest;
import com.comic.h.dto.request.RegisterRequest;
import com.comic.h.dto.response.AuthResponse;
import com.comic.h.dto.response.RegisterResponse;

public interface AuthService {

    AuthResponse login(LoginRequest userRequest);

    RegisterResponse register(RegisterRequest userRequest);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(LogoutRequest request);
}
