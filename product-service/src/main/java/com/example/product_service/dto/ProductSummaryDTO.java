package com.example.product_service.dto;

import java.math.BigDecimal;

public record ProductSummaryDTO(
        Long id,
        String name,
        String category,
        BigDecimal price,
        int stock
) {
}
