package com.example.order_service.dto;

import java.util.List;

public record CartDetailsDTO(
        Long id,
        List<CartItemDTO> items,
        int totalItems
) {}