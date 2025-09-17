package com.example.product_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductDetailsDTO(
        Long id,
        String name,
        String description,
        String category,
        BigDecimal price,
        int stock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
