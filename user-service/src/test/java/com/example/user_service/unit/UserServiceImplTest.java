package com.example.user_service.unit;

import com.example.user_service.dto.*;
import com.example.user_service.entity.User;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.client.KeycloakClient;
import com.example.user_service.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KeycloakClient keycloakClient;

    @InjectMocks
    private UserServiceImpl service;

    private RegisterRequestDto registerRequest;
    private LoginRequestDto loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDto("username", "test@example.com", "password123");
        loginRequest = new LoginRequestDto("test@example.com", "password123");
    }

    @Test
    void register_ShouldCreateUserAndReturnTokens() {
        // given
        String fakeUserId = "abc-123";
        TokenResponse tokenResponse = mockToken();

        when(keycloakClient.createUser(any(KeycloakUserRepresentation.class)))
                .thenReturn(fakeUserId);
        when(userRepository.existsById(fakeUserId))
                .thenReturn(false);
        when(keycloakClient.getUserTokens(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(tokenResponse);

        // when
        TokenResponse result = service.register(registerRequest);

        // then
        assertEquals(tokenResponse, result);
        verify(keycloakClient).createUser(any(KeycloakUserRepresentation.class));
        verify(userRepository).save(any(User.class));
    }


    @Test
    void register_ShouldNotSaveUserIfAlreadyExists() {
        // given
        String existingUserId = "xyz-999";
        when(keycloakClient.createUser(any(KeycloakUserRepresentation.class)))
                .thenReturn(existingUserId);
        when(userRepository.existsById(existingUserId))
                .thenReturn(true);
        when(keycloakClient.getUserTokens(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockToken());

        // when
        service.register(registerRequest);

        // then
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ShouldReturnTokenAndSaveUserIfNotExists() {
        // given
        String fakeUserId = "user-123";

        String fakeJwt = "eyJhbGciOiJIUzI1NiJ9." +
                "eyJzdWIiOiJ1c2VyLTEyMyJ9." +
                "signature";

        TokenResponse tokenResponse = new TokenResponse(fakeJwt, "refresh", 3600);

        when(keycloakClient.getUserTokens(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(tokenResponse);
        when(userRepository.existsById(fakeUserId))
                .thenReturn(false);

        // when
        TokenResponse result = service.login(loginRequest);

        // then
        assertEquals(tokenResponse, result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_ShouldThrowException_WhenTokenInvalid() {
        // given
        when(keycloakClient.getUserTokens(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new TokenResponse("invalid.token", "refresh", 3600));

        // when / then
        assertThrows(RuntimeException.class, () -> service.login(loginRequest));
    }

    @Test
    void getCurrentUserDetails_ShouldReturnUserDetails() {
        // given
        String userId = "user-123";
        User user = new User();
        user.setId(userId);
        user.setAddress("Warsaw");
        user.setPhoneNumber("123456789");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        UserDetailsResponseDto result = service.getCurrentUserDetails(userId, "milosz", "milosz@example.com");

        // then
        assertEquals("milosz", result.getUsername());
        assertEquals("milosz@example.com", result.getEmail());
        assertEquals("Warsaw", result.getAddress());
    }


    private TokenResponse mockToken() {
        return new TokenResponse("access123", "refresh123", 3600);
    }
}
