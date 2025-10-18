package com.example.cart_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartRequest(
        @NotNull(message = "ProductId must not be empty")
        Long productId,
        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {}
