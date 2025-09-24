package com.example.cart_service.controller;

import com.example.cart_service.config.SecurityUtils;
import com.example.cart_service.dto.AddToCartRequest;
import com.example.cart_service.dto.CartDetailsDTO;
import com.example.cart_service.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<CartDetailsDTO> getUserCart(HttpServletRequest request) {
        Long userId = SecurityUtils.getCurrentUserId(request);
        return ResponseEntity.ok(service.getUserCart(userId));
    }

    @PostMapping("/add")
    public ResponseEntity<CartDetailsDTO> addProductToCart(
            HttpServletRequest request,
            @Valid @RequestBody AddToCartRequest addRequest) {

        Long userId = SecurityUtils.getCurrentUserId(request);
        return ResponseEntity.ok(service.addProduct(userId, addRequest));
    }

    @PutMapping
    public ResponseEntity<CartDetailsDTO> updateCartItem(
            HttpServletRequest request,
            @Valid @RequestBody AddToCartRequest updateRequest) {

        Long userId = SecurityUtils.getCurrentUserId(request);
        return ResponseEntity.ok(service.updateQuantity(userId, updateRequest));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeCartItem(
            HttpServletRequest request,
            @PathVariable Long productId) {

        Long userId = SecurityUtils.getCurrentUserId(request);
        service.removeProduct(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(HttpServletRequest request) {
        Long userId = SecurityUtils.getCurrentUserId(request);
        service.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
