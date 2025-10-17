package com.example.user_service.service;

import com.example.user_service.dto.*;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;

public interface IUserService {

    TokenResponse register(RegisterRequestDto request);
    TokenResponse login(LoginRequestDto request);
    UserDetailsResponseDto getCurrentUserDetails(String userId, String username, String email);
}
