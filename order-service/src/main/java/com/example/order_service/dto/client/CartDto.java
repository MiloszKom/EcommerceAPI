package com.example.order_service.dto.client;

import java.util.List;

public record CartDto(
        Long cartId,
        String userId,
        List<CartItemDto> items
) {
}
