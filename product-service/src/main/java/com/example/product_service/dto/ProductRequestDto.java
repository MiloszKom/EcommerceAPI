package com.example.product_service.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequestDto (
        @NotBlank(message = "A product must have a name")
        @Size(max = 100, message = "Product name cannot exceed 100 characters")
        String name,

        @NotBlank(message = "A product must have a description")
        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @NotNull(message = "A product must have a price")
        @DecimalMin(value = "0.0", message = "Price cannot be negative")
        BigDecimal price,

        @NotNull(message = "A product must have stock")
        @Min(value = 0, message = "Stock cannot be negative")
        Integer stock
) {
}