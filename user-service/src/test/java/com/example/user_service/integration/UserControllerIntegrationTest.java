package com.example.user_service.integration;

import com.example.user_service.dto.UserUpdateRequestDto;
import com.example.user_service.entity.User;
import com.example.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UserControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User existingUser;
    private String validJwtToken;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("EXTERNAL_KEYCLOAK_URL", () -> "http://localhost:8080");
        registry.add("INTERNAL_KEYCLOAK_URL", () -> "http://localhost:8080");
        registry.add("API_GATEWAY_URL", () -> "http://localhost:8080");
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Create a test user
        existingUser = new User();
        existingUser.setId("test-user-id");
        existingUser.setAddress("123 Test Street");
        existingUser.setPhoneNumber("1234567890");
        userRepository.save(existingUser);
    }

    @Test
    void getCurrentUserDetails_WithValidJwt_ReturnsUserDetails() throws Exception {
        // Arrange
        String expectedUsername = "testuser";
        String expectedEmail = "test@example.com";

        // Act & Assert
        mockMvc.perform(get("/api/users/me")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j
                                .claim("sub", "test-user-id")
                                .claim("preferred_username", "testuser")
                                .claim("email", "test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(expectedUsername))
                .andExpect(jsonPath("$.email").value(expectedEmail))
                .andExpect(jsonPath("$.address").value(existingUser.getAddress()))
                .andExpect(jsonPath("$.phoneNumber").value(existingUser.getPhoneNumber()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void getCurrentUserDetails_WithoutJwt_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateCurrentUserDetails_WithValidJwtAndData_ReturnsUpdatedDetails() throws Exception {
        // Arrange
        UserUpdateRequestDto updateDto = new UserUpdateRequestDto();
        updateDto.setAddress("456 New Street");
        updateDto.setPhoneNumber("0987654321");

        // Act & Assert
        mockMvc.perform(put("/api/users/me")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j
                                .claim("sub", "test-user-id")
                                .claim("preferred_username", "testuser")
                                .claim("email", "test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.address").value("456 New Street"))
                .andExpect(jsonPath("$.phoneNumber").value("0987654321"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());

        // Verify database state
        User updatedUser = userRepository.findById("test-user-id").orElseThrow();
        assertEquals("456 New Street", updatedUser.getAddress());
        assertEquals("0987654321", updatedUser.getPhoneNumber());
    }

    @Test
    void updateCurrentUserDetails_WithInvalidData_ReturnsBadRequest() throws Exception {
        // Arrange
        UserUpdateRequestDto invalidDto = new UserUpdateRequestDto();
        invalidDto.setAddress("A".repeat(256)); // Exceeds max length
        invalidDto.setPhoneNumber("1234567890");

        // Act & Assert
        mockMvc.perform(put("/api/users/me")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j
                                .claim("sub", "test-user-id")
                                .claim("preferred_username", "testuser")
                                .claim("email", "test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCurrentUserDetails_WithoutJwt_ReturnsUnauthorized() throws Exception {
        // Arrange
        UserUpdateRequestDto updateDto = new UserUpdateRequestDto();
        updateDto.setAddress("456 New Street");
        updateDto.setPhoneNumber("0987654321");

        // Act & Assert
        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isUnauthorized());
    }
}