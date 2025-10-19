package com.example.user_service.integration;

import com.example.user_service.dto.KeycloakUserRepresentation;
import com.example.user_service.dto.LoginRequestDto;
import com.example.user_service.dto.RegisterRequestDto;
import com.example.user_service.dto.TokenResponse;
import com.example.user_service.entity.User;
import com.example.user_service.exception.KeycloakException;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.client.KeycloakClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "keycloak.enabled=false")
@AutoConfigureMockMvc
@Testcontainers
class UserControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private KeycloakClient keycloakClient;

    @Autowired
    private ObjectMapper objectMapper;

    private User existingUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        existingUser = new User();
        existingUser.setId("user-123");
        existingUser.setAddress("123 Main St");
        existingUser.setPhoneNumber("555-1234");
        existingUser.setCreatedAt(LocalDateTime.now());
        existingUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(existingUser);

        // Reset mocks
        Mockito.reset(keycloakClient);
    }

    @Test
    void getCurrentUserDetails_ShouldReturnUserDetails() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("X-User-Id", existingUser.getId())
                        .header("X-User-Username", "john_doe")
                        .header("X-User-Email", "john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("john_doe")))
                .andExpect(jsonPath("$.email", is("john@example.com")))
                .andExpect(jsonPath("$.address", is("123 Main St")));
    }

    @Test
    void getCurrentUserDetails_ShouldReturn404_WhenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("X-User-Id", "nonexistent-id")
                        .header("X-User-Username", "ghost")
                        .header("X-User-Email", "ghost@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void register_ShouldReturn400_WhenKeycloakUserCreationFails() throws Exception {
        // Arrange
        RegisterRequestDto request = new RegisterRequestDto("username","duplicate@example.com", "password123");

        Mockito.when(keycloakClient.createUser(any(KeycloakUserRepresentation.class)))
                .thenThrow(new KeycloakException("User already exists", HttpStatus.BAD_REQUEST));

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("User already exists")));

        // Verify no user was saved locally
        assert userRepository.count() == 1; // Only the existingUser from setUp
    }

    @Test
    void login_ShouldReturn401_WhenCredentialsAreInvalid() throws Exception {
        // Arrange
        LoginRequestDto request = new LoginRequestDto("user@example.com", "wrong-password");

        Mockito.when(keycloakClient.getUserTokens(
                        eq("password"),
                        eq("user@example.com"),
                        eq("wrong-password"),
                        eq("openid profile email")))
                .thenThrow(new KeycloakException("Invalid credentials", HttpStatus.UNAUTHORIZED));

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Invalid credentials")));
    }
}