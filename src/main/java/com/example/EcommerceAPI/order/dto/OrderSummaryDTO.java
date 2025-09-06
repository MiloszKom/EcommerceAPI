package com.example.EcommerceAPI.order.dto;

import com.example.EcommerceAPI.order.entity.OrderStatus;
import com.example.EcommerceAPI.user.dto.UserSummaryDTO;

import java.math.BigDecimal;

public record OrderSummaryDTO(
        long id,
        UserSummaryDTO user,
        OrderStatus status,
        BigDecimal totalPrice
) {
}
