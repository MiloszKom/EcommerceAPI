package com.example.EcommerceAPI.exception.types;

public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(Long cartId) {
      super("Cart not found with the id: " + cartId);
    }
}
