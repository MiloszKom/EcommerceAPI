package com.example.user_service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.user_service.dto.LoginRequest;
import com.example.user_service.dto.RegisterRequest;
import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.springframework.http.MediaType;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class UserControllerIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.gateway.secret}")
    private String gatewaySecret;

    private Long user1Id;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        User user1 = new User();
        user1.setUsername("john");
        user1.setEmail("john@example.com");
        user1.setPassword(passwordEncoder.encode("password"));
        user1 = userRepository.save(user1);
        user1Id = user1.getId();
    }

    @Test
    void callingEndpointWithoutInternalSecret_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsers_returnsAllUsers() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", user1Id)
                        .header("role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("john"));
    }

    @Test
    void getCurrentUser_returnsLoggedInUser() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", user1Id)
                        .header("role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user1Id))
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void register_withValidRequest_returnsCreatedAndToken() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/register")
                        .header("X-Internal-Secret", gatewaySecret)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists());

        assertTrue(userRepository.existsByUsername("newUser"));
    }

    @Test
    void register_withExistingUsername_returnsConflict() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setEmail("different@example.com");
        request.setPassword("password123");

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/register")
                        .header("X-Internal-Secret", gatewaySecret)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    void login_withValidCredentials_returnsToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password");

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/login")
                        .header("X-Internal-Secret", gatewaySecret)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_withInvalidPassword_returnsUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("login");
        request.setPassword("password");

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/login")
                        .header("X-Internal-Secret", gatewaySecret)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }
}
