package com.example.cart_service.controller;

import com.example.cart_service.dto.CartRequest;
import com.example.cart_service.dto.CartDto;
import com.example.cart_service.service.ICartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Validated
public class CartController {

    private final ICartService cartService;

    public CartController(ICartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartDto> getUserCart(
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(cartService.getUserCart(userId));
    }

    @PostMapping("/add")
    public ResponseEntity<CartDto> addProductToCart(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CartRequest cartRequest
    ) {
        return ResponseEntity.ok(cartService.addProductToCart(userId, cartRequest));
    }

    @PutMapping
    public ResponseEntity<CartDto> updateCartItem(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CartRequest updateRequest
    ) {
        return ResponseEntity.ok(cartService.updateCartItem(userId, updateRequest));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeCartItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long productId
    ) {
        cartService.removeCartItem(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(
            @RequestHeader("X-User-Id") String userId
    ) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
