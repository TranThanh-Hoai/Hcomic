package com.comic.h.identity.security.oauth2;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.comic.h.identity.security.oauth2.user.GoogleOAuth2UserInfo;
import com.comic.h.identity.security.oauth2.user.OAuth2UserInfo;
import com.comic.h.identity.security.oauth2.user.OAuth2UserInfoFactory;

class OAuth2UserInfoFactoryTest {

    @Test
    @DisplayName("Should return GoogleOAuth2UserInfo when registrationId is google")
    void testGoogleOAuth2UserInfo() {
        Map<String, Object> attributes = Map.of(
                "sub", "1234567890",
                "name", "Test User",
                "email", "test@gmail.com",
                "picture", "https://example.com/avatar.jpg"
        );

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo("google", attributes);

        assertNotNull(userInfo);
        assertInstanceOf(GoogleOAuth2UserInfo.class, userInfo);
        assertEquals("1234567890", userInfo.getId());
        assertEquals("Test User", userInfo.getName());
        assertEquals("test@gmail.com", userInfo.getEmail());
        assertEquals("https://example.com/avatar.jpg", userInfo.getImageUrl());
    }

    @Test
    @DisplayName("Should throw exception when registrationId is unsupported")
    void testUnsupportedRegistrationId() {
        Map<String, Object> attributes = Map.of("id", "123");

        assertThrows(OAuth2AuthenticationProcessingException.class, () ->
                OAuth2UserInfoFactory.getOAuth2UserInfo("facebook", attributes));
    }
}
