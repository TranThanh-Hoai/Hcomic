package com.comic.h.identity.security.oauth2;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.comic.h.identity.entity.User;
import com.comic.h.identity.enums.AuthProvider;
import com.comic.h.identity.enums.Role;
import com.comic.h.identity.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private CustomOAuth2UserService customOAuth2UserService;

    @BeforeEach
    void setUp() {
        customOAuth2UserService = new CustomOAuth2UserService(userRepository);
    }

    private OAuth2UserRequest createOAuth2UserRequest() {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("google")
                .clientId("test-client-id")
                .clientSecret("test-client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
                .userNameAttributeName("sub")
                .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "dummy-access-token",
                null,
                null
        );

        return new OAuth2UserRequest(clientRegistration, accessToken);
    }

    @Test
    @DisplayName("Should register new user when email does not exist")
    void testProcessOAuth2UserNewRegistration() {
        OAuth2UserRequest request = createOAuth2UserRequest();
        Map<String, Object> attributes = Map.of(
                "sub", "google-sub-123",
                "name", "Google User",
                "email", "newuser@gmail.com",
                "picture", "https://avatar.com/pic.jpg"
        );
        OAuth2User googleOAuth2User = new DefaultOAuth2User(null, attributes, "sub");

        when(userRepository.findByEmail("newuser@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setUserId(10L);
            return u;
        });

        OAuth2User result = customOAuth2UserService.processOAuth2User(request, googleOAuth2User);

        assertNotNull(result);
        assertInstanceOf(UserPrincipal.class, result);
        UserPrincipal principal = (UserPrincipal) result;
        assertEquals("newuser", principal.getUsername());
        assertEquals("newuser@gmail.com", principal.getEmail());
        assertEquals(Role.USER, principal.getRole());

        verify(userRepository, times(1)).save(argThat(user ->
                user.getEmail().equals("newuser@gmail.com") &&
                user.getProvider() == AuthProvider.GOOGLE &&
                user.getProviderId().equals("google-sub-123")
        ));
    }

    @Test
    @DisplayName("Should update existing user if email already exists")
    void testProcessOAuth2UserUpdateExisting() {
        OAuth2UserRequest request = createOAuth2UserRequest();
        Map<String, Object> attributes = Map.of(
                "sub", "google-sub-456",
                "name", "Updated Name",
                "email", "existing@gmail.com",
                "picture", "https://avatar.com/updated.jpg"
        );
        OAuth2User googleOAuth2User = new DefaultOAuth2User(null, attributes, "sub");

        User existingUser = User.builder()
                .userId(1L)
                .username("existinguser")
                .email("existing@gmail.com")
                .provider(AuthProvider.LOCAL)
                .role(Role.USER)
                .isBanned(false)
                .build();

        when(userRepository.findByEmail("existing@gmail.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OAuth2User result = customOAuth2UserService.processOAuth2User(request, googleOAuth2User);

        assertNotNull(result);
        UserPrincipal principal = (UserPrincipal) result;
        assertEquals("existinguser", principal.getUsername());

        verify(userRepository, times(1)).save(argThat(user ->
                user.getProvider() == AuthProvider.GOOGLE &&
                user.getProviderId().equals("google-sub-456") &&
                user.getDisplayName().equals("Updated Name")
        ));
    }

    @Test
    @DisplayName("Should throw exception if user account is banned")
    void testProcessOAuth2UserBannedThrowsException() {
        OAuth2UserRequest request = createOAuth2UserRequest();
        Map<String, Object> attributes = Map.of(
                "sub", "google-sub-789",
                "name", "Banned Guy",
                "email", "banned@gmail.com",
                "picture", "https://avatar.com/banned.jpg"
        );
        OAuth2User googleOAuth2User = new DefaultOAuth2User(null, attributes, "sub");

        User bannedUser = User.builder()
                .userId(2L)
                .username("banneduser")
                .email("banned@gmail.com")
                .isBanned(true)
                .banReason("Spamming comments")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("banned@gmail.com")).thenReturn(Optional.of(bannedUser));

        OAuth2AuthenticationProcessingException exception = assertThrows(
                OAuth2AuthenticationProcessingException.class,
                () -> customOAuth2UserService.processOAuth2User(request, googleOAuth2User)
        );

        assertTrue(exception.getMessage().contains("Tài khoản của bạn đã bị khóa"));
        verify(userRepository, never()).save(any(User.class));
    }
}
