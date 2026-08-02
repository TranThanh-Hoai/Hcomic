package com.comic.h.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.comic.h.repository.UserRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Toàn bộ luồng Authentication: Đăng ký -> Đăng nhập -> Lấy JWT Token -> Truy cập API được bảo vệ")
    void fullAuthFlow_Register_Login_AccessResource() throws Exception {
        // Step 1: Register a new user
        String registerJson = """
                {
                    "username": "flowuser",
                    "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("flowuser"))
                .andExpect(jsonPath("$.message").exists());

        // Step 2: Login with registered credentials
        String loginJson = """
                {
                    "username": "flowuser",
                    "password": "Password123!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userName").value("flowuser"))
                .andReturn();

        // Extract JWT token from login response
        String responseString = loginResult.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseString);
        String accessToken = jsonNode.get("accessToken").asString();

        // Step 3: Access protected endpoint WITH valid Bearer Token (Expect HTTP 404
        // Not Found instead of HTTP 401 Unauthorized)
        mockMvc.perform(get("/api/user/profile")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());

        // Step 4: Access protected endpoint WITHOUT Bearer Token (Expect HTTP 401
        // Unauthorized)
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isUnauthorized());
    }
}
