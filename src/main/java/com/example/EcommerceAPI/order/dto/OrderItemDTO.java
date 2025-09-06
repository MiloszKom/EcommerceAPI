package com.example.EcommerceAPI.order.dto;

import com.example.EcommerceAPI.product.dto.ProductSummaryDTO;

import java.math.BigDecimal;

public record OrderItemDTO(
        long id,
        ProductSummaryDTO product,
        int quantity,
        BigDecimal priceAtPurchase
) {
}
