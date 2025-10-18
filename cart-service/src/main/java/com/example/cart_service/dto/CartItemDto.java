package com.example.cart_service.dto;

public record CartItemDto(
        Long itemId,
        Long productId,
        int quantity
) {
}
