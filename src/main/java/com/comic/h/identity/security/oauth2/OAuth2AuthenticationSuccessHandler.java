package com.comic.h.identity.security.oauth2;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.comic.h.common.exception.BadRequestException;
import com.comic.h.common.security.JwtTokenProvider;
import com.comic.h.identity.entity.RefreshToken;
import com.comic.h.identity.entity.User;
import com.comic.h.identity.enums.AuthProvider;
import com.comic.h.identity.enums.Role;
import com.comic.h.identity.repository.UserRepository;
import com.comic.h.identity.service.RefreshTokenService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    @Value("${app.oauth2.authorized-redirect-uri:http://localhost:3000/oauth2/redirect}")
    private String defaultRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        Optional<String> redirectUri = CookieUtils.getCookie(request, HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        String targetUrl = redirectUri.orElse(defaultRedirectUri);

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        User user;

        if (oAuth2User instanceof UserPrincipal userPrincipal) {
            user = userRepository.findByUsername(userPrincipal.getUsername())
                    .orElseThrow(() -> new BadRequestException("User not found: " + userPrincipal.getUsername()));
        } else {
            // Fallback for DefaultOidcUser or DefaultOAuth2User
            String email = oAuth2User.getAttribute("email");
            if (email == null) {
                throw new BadRequestException("Email not found from OAuth2 provider");
            }
            user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setDisplayName(oAuth2User.getAttribute("name"));
                newUser.setAvatar(oAuth2User.getAttribute("picture"));
                newUser.setProvider(AuthProvider.GOOGLE);
                newUser.setProviderId(oAuth2User.getAttribute("sub"));
                newUser.setRole(Role.USER);
                newUser.setIsBanned(false);

                String baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "");
                if (baseUsername.isEmpty()) {
                    baseUsername = "user";
                }
                String username = baseUsername;
                int suffix = 1;
                while (userRepository.existsByUsername(username)) {
                    username = baseUsername + suffix++;
                }
                newUser.setUsername(username);
                return userRepository.save(newUser);
            });
        }

        String accessToken = jwtTokenProvider.generateTokenFromUsername(user.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", accessToken)
                .queryParam("refreshToken", refreshToken.getToken())
                .build().toUriString();
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}
