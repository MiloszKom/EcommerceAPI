package com.example.user_service.dto;

import java.time.LocalDateTime;

public record UserDetailsDTO(
        long id,
        String username,
        String email,
        LocalDateTime createdAt
) {
}
