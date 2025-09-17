package com.example.cart_service.dto;

import java.math.BigDecimal;

public record ProductDTO (
        Long id,
        String name,
        String description,
        String category,
        BigDecimal price,
        int stock
) {
}
