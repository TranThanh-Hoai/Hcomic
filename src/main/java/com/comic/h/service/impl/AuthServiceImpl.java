package com.comic.h.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.comic.h.dto.request.LoginRequest;
import com.comic.h.dto.request.RegisterRequest;
import com.comic.h.dto.response.AuthResponse;
import com.comic.h.dto.response.RegisterResponse;
import com.comic.h.entity.Role;
import com.comic.h.entity.User;
import com.comic.h.repository.UserRepository;
import com.comic.h.security.JwtTokenProvider;
import com.comic.h.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final JwtTokenProvider jwtTokenProvider;

        @Override
        public RegisterResponse register(RegisterRequest userRequest) {
                if (userRepository.existsByUserName(userRequest.getUsername())) {
                        throw new RuntimeException("Username is already taken!");
                }

                User userRegister = new User();
                userRegister.setUserName(userRequest.getUsername());
                userRegister.setUserPassword(passwordEncoder.encode(userRequest.getPassword()));
                userRegister.setUserRole(Role.USER);

                userRepository.save(userRegister);

                return RegisterResponse.builder()
                                .message("Register successful! Have a good day")
                                .userName(userRegister.getUserName())
                                .userRole(userRegister.getUserRole())
                                .build();
        }

        @Override
        public AuthResponse login(LoginRequest userRequest) {
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                userRequest.getUsername(),
                                                userRequest.getPassword()));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                String token = jwtTokenProvider.generateToken(authentication);

                User user = userRepository.findByUserName(userRequest.getUsername())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                return AuthResponse.builder()
                                .message("Welcome! " + user.getUserName())
                                .accessToken(token)
                                .tokenType("Bearer")
                                .userName(user.getUserName())
                                .userRole(user.getUserRole())
                                .build();
        }
}
