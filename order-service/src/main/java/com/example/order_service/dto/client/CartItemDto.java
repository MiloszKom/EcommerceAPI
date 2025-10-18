package com.example.order_service.dto.client;

public record CartItemDto(
        Long itemId,
        Long productId,
        int quantity
) {
}
