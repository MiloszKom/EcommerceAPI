package com.example.order_service.dto;

import java.math.BigDecimal;

public record OrderItemDto(
        long id,
        long productId,
        String productName,
        int quantity,
        BigDecimal priceAtPurchase
) {
}
