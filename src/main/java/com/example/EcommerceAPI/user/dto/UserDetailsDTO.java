package com.example.EcommerceAPI.user.dto;

import com.example.EcommerceAPI.cart.dto.CartSummaryDTO;

import java.time.LocalDateTime;

public record UserDetailsDTO(
        long id,
        String username,
        String email,
        CartSummaryDTO cartSummary,
        LocalDateTime createdAt
) {
}
