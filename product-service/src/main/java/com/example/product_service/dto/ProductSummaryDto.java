package com.example.product_service.dto;

import java.math.BigDecimal;

public record ProductSummaryDto(
        Long id,
        String name,
        BigDecimal price,
        int stock
) {
}
