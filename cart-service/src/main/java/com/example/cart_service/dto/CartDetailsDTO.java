package com.example.cart_service.dto;

import java.util.List;

public record CartDetailsDTO(
        long id,
        List<CartItemDTO> items,
        int totalItems
) {
}
