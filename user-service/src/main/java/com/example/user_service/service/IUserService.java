package com.example.user_service.service;

import com.example.user_service.dto.*;

public interface IUserService {

    UserDetailsResponseDto getCurrentUserDetails(String userId, String username, String email);
    UserDetailsResponseDto updateUserDetails(String userId, String username, String email, UserUpdateRequestDto updateDto);
}
