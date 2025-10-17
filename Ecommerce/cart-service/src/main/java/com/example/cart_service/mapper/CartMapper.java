package com.example.cart_service.mapper;

import com.example.cart_service.dto.CartItemDto;
import com.example.cart_service.dto.CartDto;
import com.example.cart_service.entity.Cart;
import com.example.cart_service.entity.CartItem;

import java.util.List;
import java.util.stream.Collectors;

public class CartMapper {
    public static CartDto mapToCartDto(Cart cart) {
        List<CartItemDto> items = cart.getItems().stream()
                .map(CartMapper::mapToCartItemResponseDto)
                .collect(Collectors.toList());

        return new CartDto(
                cart.getId(),
                cart.getUserId(),
                items
        );
    }

    private static CartItemDto mapToCartItemResponseDto(CartItem item) {
        return new CartItemDto(
                item.getId(),
                item.getProductId(),
                item.getQuantity()
        );
    }
}
