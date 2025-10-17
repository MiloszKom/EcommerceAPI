package com.example.cart_service.dto;

import java.util.List;

public record CartDto(
        Long cartId,
        String userId,
        List<CartItemDto> items
) {
}
