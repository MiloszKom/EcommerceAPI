package com.example.cart_service.service;

import com.example.cart_service.dto.CartRequest;
import com.example.cart_service.dto.CartDto;
import com.example.cart_service.dto.UpdateCartItemRequest;
import com.example.cart_service.entity.Cart;

public interface ICartService {

    CartDto getUserCart(String userId);

    CartDto addProductToCart(String userId, CartRequest cartRequest);
    CartDto updateCartItem(String userId, Long itemId, UpdateCartItemRequest cartRequest);
    CartDto removeCartItem(String userId, Long itemId);
    CartDto clearCart(String userId);
}
