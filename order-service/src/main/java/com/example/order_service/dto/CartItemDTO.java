package com.example.order_service.dto;

public record CartItemDTO (
        Long productId,
        int quantity
){
}
