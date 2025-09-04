package com.example.EcommerceAPI.cart.dto;

import java.math.BigDecimal;

public record CartSummaryDTO(
        BigDecimal totalPrice,
        int totalItems
) {
}
