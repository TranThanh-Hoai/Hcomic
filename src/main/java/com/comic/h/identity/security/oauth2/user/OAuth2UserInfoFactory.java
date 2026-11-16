package com.comic.h.identity.security.oauth2.user;

import java.util.Map;

import com.comic.h.identity.enums.AuthProvider;
import com.comic.h.identity.security.oauth2.OAuth2AuthenticationProcessingException;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase(AuthProvider.GOOGLE.name())) {
            return new GoogleOAuth2UserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationProcessingException("Login with " + registrationId + " is not supported yet.");
        }
    }
}
