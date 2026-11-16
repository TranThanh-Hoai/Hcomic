package com.comic.h.identity.security.oauth2;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import jakarta.servlet.http.Cookie;

class HttpCookieOAuth2AuthorizationRequestRepositoryTest {

    private HttpCookieOAuth2AuthorizationRequestRepository repository;

    @BeforeEach
    void setUp() {
        repository = new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    @Test
    @DisplayName("Should save and load authorization request from cookies")
    void testSaveAndLoadAuthorizationRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        OAuth2AuthorizationRequest authRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .clientId("test-client-id")
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                .state("sample-state-123")
                .build();

        repository.saveAuthorizationRequest(authRequest, request, response);

        Cookie authCookie = response.getCookie(HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        assertNotNull(authCookie);
        assertTrue(authCookie.isHttpOnly());

        // Simulate incoming request with cookie
        request.setCookies(authCookie);
        OAuth2AuthorizationRequest loadedRequest = repository.loadAuthorizationRequest(request);

        assertNotNull(loadedRequest);
        assertEquals("test-client-id", loadedRequest.getClientId());
        assertEquals("sample-state-123", loadedRequest.getState());
        assertEquals("https://accounts.google.com/o/oauth2/v2/auth", loadedRequest.getAuthorizationUri());
    }

    @Test
    @DisplayName("Should remove authorization request cookies")
    void testRemoveAuthorizationRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        Cookie authCookie = new Cookie(HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, "dummy");
        request.setCookies(authCookie);

        repository.removeAuthorizationRequestCookies(request, response);

        Cookie deletedCookie = response.getCookie(HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        assertNotNull(deletedCookie);
        assertEquals(0, deletedCookie.getMaxAge());
    }
}
