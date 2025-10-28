package com.example.user_service.mapper;

import com.example.user_service.dto.UserDetailsResponseDto;
import com.example.user_service.entity.User;

public class UserMapper {

    public static UserDetailsResponseDto mapToUserDetailsResponseDto(User user, String username, String email) {
        return new UserDetailsResponseDto(
                username,
                email,
                user.getAddress(),
                user.getPhoneNumber(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
