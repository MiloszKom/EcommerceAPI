package com.example.EcommerceAPI.exception.types;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(long productId) {
        super("Product not found with the id: " + productId);
    }
}
