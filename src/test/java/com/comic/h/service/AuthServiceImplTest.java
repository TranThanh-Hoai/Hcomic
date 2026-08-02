package com.comic.h.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.comic.h.dto.request.LoginRequest;
import com.comic.h.dto.request.RegisterRequest;
import com.comic.h.dto.response.AuthResponse;
import com.comic.h.dto.response.RegisterResponse;
import com.comic.h.entity.Role;
import com.comic.h.entity.User;
import com.comic.h.repository.UserRepository;
import com.comic.h.security.JwtTokenProvider;
import com.comic.h.service.impl.AuthServiceImpl;

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
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("register - Đăng ký thành công trả về RegisterResponse")
    void register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");

        when(userRepository.existsByUserName("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        RegisterResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("newuser", response.getUserName());
        assertEquals(Role.USER, response.getUserRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register - Tên đăng nhập đã tồn tại -> Ném ra RuntimeException")
    void register_UsernameAlreadyTaken_ShouldThrowException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");

        when(userRepository.existsByUserName("existinguser")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authService.register(request)
        );

        assertEquals("Username is already taken!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("login - Đăng nhập thành công trả về AuthResponse chứa JWT Token")
    void login_Success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password123");

        User user = new User();
        user.setUserName("john");
        user.setUserRole(Role.USER);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt.token.string");
        when(userRepository.findByUserName("john")).thenReturn(Optional.of(user));

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt.token.string", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("john", response.getUserName());
        assertEquals(Role.USER, response.getUserRole());
    }

    @Test
    @DisplayName("login - Không tìm thấy user trong DB sau khi authenticate -> Ném ra RuntimeException")
    void login_UserNotFound_ShouldThrowException() {
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt.token.string");
        when(userRepository.findByUserName("john")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authService.login(request)
        );

        assertEquals("User not found", exception.getMessage());
    }
}
