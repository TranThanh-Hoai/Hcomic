package com.comic.h.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comic.h.dto.request.LoginRequest;
import com.comic.h.dto.request.LogoutRequest;
import com.comic.h.dto.request.RefreshTokenRequest;
import com.comic.h.dto.request.RegisterRequest;
import com.comic.h.dto.response.AuthResponse;
import com.comic.h.dto.response.RegisterResponse;
import com.comic.h.entity.RefreshToken;
import com.comic.h.entity.User;
import com.comic.h.enums.Role;
import com.comic.h.exception.BadRequestException;
import com.comic.h.exception.ResourceNotFoundException;
import com.comic.h.repository.UserRepository;
import com.comic.h.security.JwtTokenProvider;
import com.comic.h.service.AuthService;
import com.comic.h.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final JwtTokenProvider jwtTokenProvider;
        private final RefreshTokenService refreshTokenService;

        @Override
        public RegisterResponse register(RegisterRequest userRequest) {
                if (userRepository.existsByUsername(userRequest.getUsername())) {
                        throw new BadRequestException("Username is already taken!");
                }

                User userRegister = new User();
                userRegister.setUsername(userRequest.getUsername());
                userRegister.setPassword(passwordEncoder.encode(userRequest.getPassword()));
                userRegister.setRole(Role.USER);

                userRepository.save(userRegister);

                return RegisterResponse.builder()
                                .message("Register successful! Have a good day")
                                .userName(userRegister.getUsername())
                                .userRole(userRegister.getRole())
                                .build();
        }

        @Override
        @Transactional
        public AuthResponse login(LoginRequest userRequest) {
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                userRequest.getUsername(),
                                                userRequest.getPassword()));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                User user = userRepository.findByUsername(userRequest.getUsername())
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                String accessToken = jwtTokenProvider.generateToken(authentication);
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

                return AuthResponse.builder()
                                .message("Welcome! " + user.getUsername())
                                .accessToken(accessToken)
                                .refreshToken(refreshToken.getToken())
                                .tokenType("Bearer")
                                .username(user.getUsername())
                                .userRole(user.getRole())
                                .build();
        }

        @Override
        @Transactional
        public AuthResponse refreshToken(RefreshTokenRequest request) {
                String requestRefreshToken = request.getRefreshToken();

                return refreshTokenService.findByToken(requestRefreshToken)
                                .map(refreshTokenService::verifyExpiration)
                                .map(RefreshToken::getUser)
                                .map(user -> {
                                        String newAccessToken = jwtTokenProvider.generateTokenFromUsername(user.getUsername());
                                        // Refresh Token Rotation: generate a new refresh token and delete the old one
                                        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

                                        return AuthResponse.builder()
                                                        .message("Token refreshed successfully")
                                                        .accessToken(newAccessToken)
                                                        .refreshToken(newRefreshToken.getToken())
                                                        .tokenType("Bearer")
                                                        .username(user.getUsername())
                                                        .userRole(user.getRole())
                                                        .build();
                                })
                                .orElseThrow(() -> new BadRequestException("Refresh token is not found in database!"));
        }

        @Override
        @Transactional
        public void logout(LogoutRequest request) {
                String refreshToken = request.getRefreshToken();
                refreshTokenService.deleteByToken(refreshToken);
        }
}
