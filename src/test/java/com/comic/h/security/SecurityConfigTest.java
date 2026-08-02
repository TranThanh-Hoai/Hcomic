package com.comic.h.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Public Endpoint /api/auth/hello được phép truy cập mà không cần token (HTTP 200)")
    void publicEndpoint_Hello_ShouldBePermittedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/auth/hello"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Public Endpoint /api/auth/register nhận request rỗng không bị chặn lỗi 401 (HTTP 400 Bad Request)")
    void publicEndpoint_Register_ShouldBePermittedWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Public Endpoint /api/auth/login nhận request rỗng không bị chặn lỗi 401 (HTTP 400 Bad Request)")
    void publicEndpoint_Login_ShouldBePermittedWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Protected Endpoint bất kỳ khi không có Token sẽ trả về 401 Unauthorized")
    void protectedEndpoint_WithoutToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/protected/profile"))
                .andExpect(status().isUnauthorized());
    }
}
