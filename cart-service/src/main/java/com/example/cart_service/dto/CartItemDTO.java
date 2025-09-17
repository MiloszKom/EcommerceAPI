package com.example.cart_service.dto;

public record CartItemDTO(
        long productId,
        int quantity
) {
}