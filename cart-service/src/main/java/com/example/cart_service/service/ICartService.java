package com.example.cart_service.service;

import com.example.cart_service.dto.CartRequest;
import com.example.cart_service.dto.CartDto;

public interface ICartService {

    CartDto getUserCart(String userId);

    CartDto addProductToCart(String userId, CartRequest cartRequest);
    CartDto updateCartItem(String userId, CartRequest cartRequest);
    CartDto removeCartItem(String userId, Long productId);
    CartDto clearCart(String userId);
}
