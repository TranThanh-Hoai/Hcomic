package com.comic.h.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.comic.h.dto.request.LoginRequest;
import com.comic.h.dto.request.LogoutRequest;
import com.comic.h.dto.request.RefreshTokenRequest;
import com.comic.h.dto.request.RegisterRequest;
import com.comic.h.dto.response.AuthResponse;
import com.comic.h.dto.response.RegisterResponse;
import com.comic.h.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest userRequest) {
        return ResponseEntity.ok(userService.register(userRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest userRequest) {
        return ResponseEntity.ok(userService.login(userRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(userService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody LogoutRequest request) {
        userService.logout(request);
        return ResponseEntity.ok(Map.of("message", "Logout successful!"));
    }

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }
}
