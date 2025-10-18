package com.example.product_service.dto;

import jakarta.validation.constraints.Min;

public record StockUpdateRequest(
        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {
}
