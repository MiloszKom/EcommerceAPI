package com.example.EcommerceAPI.exception.types;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long orderId) {
         super("Order not found with the id: " + orderId);
    }
}
