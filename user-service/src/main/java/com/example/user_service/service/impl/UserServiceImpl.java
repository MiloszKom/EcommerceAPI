package com.example.user_service.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.user_service.dto.*;
import com.example.user_service.entity.User;
import com.example.user_service.exception.ResourceNotFoundException;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.IUserService;

import com.example.user_service.service.client.KeycloakClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements IUserService {

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

        saveLocalUserIfNotExists(userId);

        return keycloakClient.getUserTokens(
                "password",
                request.getEmail(),
                request.getPassword(),
                "openid profile email");
    }

    @Override
    public TokenResponse login(LoginRequestDto request) {
        TokenResponse token = keycloakClient.getUserTokens("password", request.getEmail(), request.getPassword(), "openid profile email");
        try {
            DecodedJWT jwt = JWT.decode(token.getAccessToken());
            String userId = jwt.getSubject();
            saveLocalUserIfNotExists(userId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract user ID from token: " + e.getMessage(), e);
        }
        return token;
    }

    @Override
    public UserDetailsResponseDto getCurrentUserDetails(String userId, String username, String email) {
        User localUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

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