package com.example.EcommerceAPI.order.dto;

import com.example.EcommerceAPI.order.entity.OrderStatus;
import com.example.EcommerceAPI.user.dto.UserSummaryDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailsDTO(
        long id,
        UserSummaryDTO user,
        List<OrderItemDTO> items,
        OrderStatus status,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
