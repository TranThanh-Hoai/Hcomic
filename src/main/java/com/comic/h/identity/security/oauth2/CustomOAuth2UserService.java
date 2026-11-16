package com.comic.h.identity.security.oauth2;

import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.comic.h.identity.entity.User;
import com.comic.h.identity.enums.AuthProvider;
import com.comic.h.identity.enums.Role;
import com.comic.h.identity.repository.UserRepository;
import com.comic.h.identity.security.oauth2.user.OAuth2UserInfo;
import com.comic.h.identity.security.oauth2.user.OAuth2UserInfoFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (Exception ex) {
            log.error("Error processing OAuth2 user: {}", ex.getMessage(), ex);
            throw new OAuth2AuthenticationProcessingException(ex.getMessage(), ex);
        }
    }

    OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();

            if (Boolean.TRUE.equals(user.getIsBanned())) {
                throw new OAuth2AuthenticationProcessingException("Tài khoản của bạn đã bị khóa: " + user.getBanReason());
            }

            user = updateExistingUser(user, oAuth2UserInfo, registrationId);
        } else {
            user = registerNewUser(oAuth2UserInfo, registrationId);
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserInfo oAuth2UserInfo, String registrationId) {
        User user = new User();
        user.setProvider(AuthProvider.valueOf(registrationId.toUpperCase()));
        user.setProviderId(oAuth2UserInfo.getId());
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setDisplayName(oAuth2UserInfo.getName());
        user.setAvatar(oAuth2UserInfo.getImageUrl());
        user.setRole(Role.USER);
        user.setIsBanned(false);

        String baseUsername = oAuth2UserInfo.getEmail().split("@")[0].replaceAll("[^a-zA-Z0-9_]", "");
        if (baseUsername.isEmpty()) {
            baseUsername = "user";
        }
        String username = baseUsername;
        int suffix = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + suffix++;
        }
        user.setUsername(username);

        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo, String registrationId) {
        // Link provider if not yet set
        if (existingUser.getProvider() == null || existingUser.getProvider() == AuthProvider.LOCAL) {
            existingUser.setProvider(AuthProvider.valueOf(registrationId.toUpperCase()));
            existingUser.setProviderId(oAuth2UserInfo.getId());
        }

        if (StringUtils.hasText(oAuth2UserInfo.getName()) && !StringUtils.hasText(existingUser.getDisplayName())) {
            existingUser.setDisplayName(oAuth2UserInfo.getName());
        }

        if (existingUser.getAvatar() == null || existingUser.getAvatar().startsWith("http")) {
            existingUser.setAvatar(oAuth2UserInfo.getImageUrl());
        }

        return userRepository.save(existingUser);
    }
}
