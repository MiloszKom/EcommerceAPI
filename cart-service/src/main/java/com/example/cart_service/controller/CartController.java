package com.example.cart_service.controller;

import com.example.cart_service.dto.AddToCartRequest;
import com.example.cart_service.dto.CartDetailsDTO;
import com.example.cart_service.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService service;

    @GetMapping
    public ResponseEntity<CartDetailsDTO> getUserCart() {
        CartDetailsDTO cart = service.getUserCart();
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add")
    public ResponseEntity<CartDetailsDTO> addProductToCart(@Valid @RequestBody AddToCartRequest request) {
        CartDetailsDTO updatedCart = service.addProduct(request);
        return ResponseEntity.ok(updatedCart);
    }


    @PutMapping
    public ResponseEntity<CartDetailsDTO> updateCartItem(@Valid @RequestBody AddToCartRequest request) {
        CartDetailsDTO updatedCart = service.updateQuantity(request);
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeCartItem(@PathVariable Long productId) {
        service.removeProduct(productId);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart() {
        service.clearCart();
        return ResponseEntity.noContent().build();
    }
}
