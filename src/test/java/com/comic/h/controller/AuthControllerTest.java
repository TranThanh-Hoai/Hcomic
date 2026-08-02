package com.comic.h.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.comic.h.dto.request.LoginRequest;
import com.comic.h.dto.request.RegisterRequest;
import com.comic.h.dto.response.AuthResponse;
import com.comic.h.dto.response.RegisterResponse;
import com.comic.h.entity.Role;
import com.comic.h.service.AuthService;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private AuthService authService;

        @Test
        @DisplayName("POST /api/auth/register - Đăng ký thành công trả về HTTP 200 OK")
        void register_Success_ShouldReturn200() throws Exception {
                RegisterRequest request = new RegisterRequest();
                request.setUsername("newuser");
                request.setPassword("password123");

                RegisterResponse response = RegisterResponse.builder()
                                .message("Register successful! Have a good day")
                                .userName("newuser")
                                .userRole(Role.USER)
                                .build();

                when(authService.register(any(RegisterRequest.class))).thenReturn(response);

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.userName").value("newuser"))
                                .andExpect(jsonPath("$.message").value("Register successful! Have a good day"));
        }

        @Test
        @DisplayName("POST /api/auth/register - Đăng ký thất bại trả về HTTP 400 Bad Request")
        void register_Failure_ShouldReturn400() throws Exception {
                RegisterRequest request = new RegisterRequest();
                request.setUsername("existinguser");
                request.setPassword("password123");

                when(authService.register(any(RegisterRequest.class)))
                                .thenThrow(new RuntimeException("Username is already taken!"));

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Username is already taken!"));
        }

        @Test
        @DisplayName("POST /api/auth/login - Đăng nhập thành công trả về HTTP 200 OK kèm Token")
        void login_Success_ShouldReturn200() throws Exception {
                LoginRequest request = new LoginRequest();
                request.setUsername("john");
                request.setPassword("password123");

                AuthResponse response = AuthResponse.builder()
                                .message("Welcome! john")
                                .accessToken("mocked.jwt.token")
                                .tokenType("Bearer")
                                .username("john")
                                .userRole(Role.USER)
                                .build();

                when(authService.login(any(LoginRequest.class))).thenReturn(response);

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").value("mocked.jwt.token"))
                                .andExpect(jsonPath("$.userName").value("john"));
        }

        @Test
        @DisplayName("POST /api/auth/login - Đăng nhập thất bại trả về HTTP 400 Bad Request")
        void login_Failure_ShouldReturn400() throws Exception {
                LoginRequest request = new LoginRequest();
                request.setUsername("john");
                request.setPassword("wrongpass");

                when(authService.login(any(LoginRequest.class)))
                                .thenThrow(new RuntimeException("Bad credentials"));

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Bad credentials"));
        }

        @Test
        @DisplayName("GET /api/auth/hello - Trả về string 'hello'")
        void hello_ShouldReturnString() throws Exception {
                mockMvc.perform(get("/api/auth/hello"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("hello"));
        }
}
