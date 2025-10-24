package com.example.user_service.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.user_service.controller.UserController;
import com.example.user_service.dto.*;
import com.example.user_service.entity.User;
import com.example.user_service.exception.ResourceNotFoundException;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.IUserService;

import com.example.user_service.service.client.KeycloakClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements IUserService {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;
    private final KeycloakClient keycloakClient;

    public UserServiceImpl(
            UserRepository userRepository,
            KeycloakClient keycloakClient
    ) {
        this.userRepository = userRepository;
        this.keycloakClient = keycloakClient;
    }

    @Override
    public TokenResponse register(RegisterRequestDto request) {
        KeycloakUserRepresentation userRep = new KeycloakUserRepresentation();
        userRep.setUsername(request.getEmail());
        userRep.setEmail(request.getEmail());
        userRep.setCredentials(List.of(new CredentialRepresentation("password", request.getPassword(), false)));

        String userId = keycloakClient.createUser(userRep);
        log.info("Keycloak user created successfully with ID: {}", userId);
        saveLocalUserIfNotExists(userId);
        log.info("Local user saved successfully for ID: {}", userId);
        TokenResponse token = keycloakClient.getUserTokens(
                "password",
                request.getEmail(),
                request.getPassword(),
                "openid profile email"
        );
        log.debug("Access token retrieved for new user ID: {}", userId);

        return token;
    }

    @Override
    public TokenResponse login(LoginRequestDto request) {
        log.debug("Attempting login for email: {}", request.getEmail());
        TokenResponse token = keycloakClient.getUserTokens("password", request.getEmail(), request.getPassword(), "openid profile email");
        try {
            DecodedJWT jwt = JWT.decode(token.getAccessToken());
            String userId = jwt.getSubject();
            log.debug("Extracted user ID {} from token", userId);
            saveLocalUserIfNotExists(userId);
            log.info("User login successful for userId: {}", userId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract user ID from token: " + e.getMessage(), e);
        }

        return token;
    }

    @Override
    public UserDetailsResponseDto getCurrentUserDetails(String userId, String username, String email) {
        log.debug("Fetching user details for userId: {}", userId);
        User localUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        log.info("Returning details for userId: {}", userId);
        return new UserDetailsResponseDto(
                username,
                email,
                localUser.getAddress(),
                localUser.getPhoneNumber(),
                localUser.getCreatedAt(),
                localUser.getUpdatedAt()
        );
    }

    private void saveLocalUserIfNotExists(String userId) {
        if (!userRepository.existsById(userId)) {
            User newUser = new User();
            newUser.setId(userId);
            userRepository.save(newUser);
        }
    }
}