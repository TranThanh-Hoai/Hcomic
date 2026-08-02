package com.comic.h.security;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "SuperComicSpringBootCreateByAiVeryStrongSecretKeyForJwtAuthentication2026";
    private final long expiration = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationDate", expiration);
    }

    @Test
    @DisplayName("generateToken - Tạo token JWT thành công từ đối tượng Authentication")
    void generateToken_ShouldReturnValidToken() {
        User userDetails = new User("testuser", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        String token = jwtTokenProvider.generateToken(authentication);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("getUsername - Trích xuất chính xác username từ token JWT")
    void getUsername_ShouldExtractCorrectUsername() {
        User userDetails = new User("testuser", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        String token = jwtTokenProvider.generateToken(authentication);

        String username = jwtTokenProvider.getUsername(token);

        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("validateToken - Trả về true khi token JWT hợp lệ")
    void validateToken_WithValidToken_ShouldReturnTrue() {
        User userDetails = new User("testuser", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        String token = jwtTokenProvider.generateToken(authentication);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("validateToken - Trả về false khi token đã hết hạn")
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        // Set expiration to negative value (-1ms)
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationDate", -1000L);
        User userDetails = new User("testuser", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        String expiredToken = jwtTokenProvider.generateToken(authentication);

        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("validateToken - Trả về false khi token sai định dạng (malformed)")
    void validateToken_WithMalformedToken_ShouldReturnFalse() {
        String malformedToken = "invalid.token.string";

        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        assertFalse(isValid);
    }
}
