package com.example.EcommerceAPI.cart.dto;

import com.example.EcommerceAPI.product.dto.ProductSummaryDTO;

public record CartItemDTO(
        ProductSummaryDTO product,
        int quantity
) {
}
