package com.example.order_service.dto;

import com.example.order_service.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryDTO(
        long id,
        OrderStatus status,
        BigDecimal totalPrice,
        int numberOfItems,
        LocalDateTime createdAt
) {}

