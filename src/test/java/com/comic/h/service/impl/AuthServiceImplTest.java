package com.comic.h.service.impl;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.comic.h.dto.request.LoginRequest;
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
import com.comic.h.service.RefreshTokenService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ==========================================
    // 1. REGISTER TESTS
    // ==========================================

    @Test
    @DisplayName("Register - Thành công khi username chưa tồn tại")
    void register_Success() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUserName()).isEqualTo("testuser");
        assertThat(response.getUserRole()).isEqualTo(Role.USER);
        assertThat(response.getMessage()).isEqualTo("Register successful! Have a good day");

        verify(userRepository).existsByUsername("testuser");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("testuser")
                        && user.getPassword().equals("encodedPassword")
                        && user.getRole() == Role.USER
        ));
    }

    @Test
    @DisplayName("Register - Ném BadRequestException khi username đã tồn tại")
    void register_DuplicateUsername_ThrowsBadRequestException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Username is already taken!");

        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    // ==========================================
    // 2. LOGIN TESTS
    // ==========================================

    @Test
    @DisplayName("Login - Thành công với thông tin xác thực chính xác")
    void login_Success() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("comicfan");
        request.setPassword("secretPass");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        User user = User.builder()
                .userId(1L)
                .username("comicfan")
                .role(Role.USER)
                .build();

        when(userRepository.findByUsername("comicfan")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("mocked-jwt-access-token");

        RefreshToken refreshToken = RefreshToken.builder()
                .token("mocked-refresh-token-uuid")
                .user(user)
                .build();
        when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("comicfan");
        assertThat(response.getAccessToken()).isEqualTo("mocked-jwt-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("mocked-refresh-token-uuid");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUserRole()).isEqualTo(Role.USER);
        assertThat(response.getMessage()).isEqualTo("Welcome! comicfan");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("comicfan");
        verify(jwtTokenProvider).generateToken(authentication);
        verify(refreshTokenService).createRefreshToken(user);
    }

    @Test
    @DisplayName("Login - Ném ResourceNotFoundException khi xác thực xong nhưng user không tồn tại trong DB")
    void login_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("ghostuser");
        request.setPassword("password123");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("ghostuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(jwtTokenProvider, never()).generateToken(any());
        verify(refreshTokenService, never()).createRefreshToken(any());
    }

    // ==========================================
    // 3. REFRESH TOKEN ROTATION TESTS
    // ==========================================

    @Test
    @DisplayName("Refresh Token - Rotation thành công cấp Access Token mới và Refresh Token mới")
    void refreshToken_Success() {
        // Arrange
        String oldTokenString = "valid-old-refresh-token";
        RefreshTokenRequest request = new RefreshTokenRequest(oldTokenString);

        User user = User.builder()
                .userId(1L)
                .username("comicfan")
                .role(Role.USER)
                .build();

        RefreshToken oldRefreshToken = RefreshToken.builder()
                .token(oldTokenString)
                .user(user)
                .build();

        RefreshToken newRefreshToken = RefreshToken.builder()
                .token("new-rotated-refresh-token")
                .user(user)
                .build();

        when(refreshTokenService.findByToken(oldTokenString)).thenReturn(Optional.of(oldRefreshToken));
        when(refreshTokenService.verifyExpiration(oldRefreshToken)).thenReturn(oldRefreshToken);
        when(jwtTokenProvider.generateTokenFromUsername("comicfan")).thenReturn("new-jwt-access-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(newRefreshToken);

        // Act
        AuthResponse response = authService.refreshToken(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-jwt-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-rotated-refresh-token");
        assertThat(response.getUsername()).isEqualTo("comicfan");
        assertThat(response.getUserRole()).isEqualTo(Role.USER);
        assertThat(response.getMessage()).isEqualTo("Token refreshed successfully");

        verify(refreshTokenService).findByToken(oldTokenString);
        verify(refreshTokenService).verifyExpiration(oldRefreshToken);
        verify(jwtTokenProvider).generateTokenFromUsername("comicfan");
        verify(refreshTokenService).createRefreshToken(user);
    }

    @Test
    @DisplayName("Refresh Token - Ném BadRequestException khi token không tồn tại trong database")
    void refreshToken_NotFound_ThrowsBadRequestException() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest("non-existent-token");
        when(refreshTokenService.findByToken("non-existent-token")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Refresh token is not found in database!");

        verify(jwtTokenProvider, never()).generateTokenFromUsername(any());
        verify(refreshTokenService, never()).createRefreshToken(any());
    }
}
