package com.example.EcommerceAPI.cart.dto;

import com.example.EcommerceAPI.user.dto.UserSummaryDTO;

import java.math.BigDecimal;
import java.util.List;

public record CartDetailsDTO(
        long id,
        List<CartItemDTO> items,
        BigDecimal totalPrice,
        int totalItems
) {
}
