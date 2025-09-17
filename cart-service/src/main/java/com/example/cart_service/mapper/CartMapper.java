package com.example.cart_service.mapper;

import com.example.cart_service.dto.CartDetailsDTO;
import com.example.cart_service.dto.CartItemDTO;
import com.example.cart_service.model.Cart;
import com.example.cart_service.model.CartItem;

import java.util.List;

public class CartMapper {
    public static CartDetailsDTO toCartDetailsDTO(Cart cart) {
        List<CartItemDTO> items = cart.getItems().stream()
                .map(CartMapper::toCartItemDTO)
                .toList();

        return new CartDetailsDTO(
                cart.getId(),
                items,
                cart.getItems().stream().mapToInt(CartItem::getQuantity).sum()
        );
    }

    private static CartItemDTO toCartItemDTO(CartItem item) {
        return new CartItemDTO(item.getProductId(), item.getQuantity());
    }
}
