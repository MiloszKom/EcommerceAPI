package com.example.cart_service.controller;

import com.example.cart_service.dto.CartRequest;
import com.example.cart_service.dto.CartDto;
import com.example.cart_service.service.ICartService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Validated
public class CartController {
    private static final Logger log = LoggerFactory.getLogger(CartController.class);
    private final ICartService cartService;

    public CartController(ICartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartDto> getUserCart(
            @RequestHeader("X-User-Id") String userId
    ) {
        log.debug("Received request to fetch cart for userId={}", userId);
        return ResponseEntity.ok(cartService.getUserCart(userId));
    }

    @PostMapping("/add")
    public ResponseEntity<CartDto> addProductToCart(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CartRequest cartRequest
    ) {
        log.info("UserId={} is adding productId={} (quantity={}) to cart",
                userId, cartRequest.productId(), cartRequest.quantity());

        return ResponseEntity.ok(cartService.addProductToCart(userId, cartRequest));
    }

    @PutMapping
    public ResponseEntity<CartDto> updateCartItem(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CartRequest updateRequest
    ) {
        log.info("UserId={} is updating productId={} in cart (new quantity={})",
                userId, updateRequest.productId(), updateRequest.quantity());

        return ResponseEntity.ok(cartService.updateCartItem(userId, updateRequest));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeCartItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long productId
    ) {
        log.info("UserId={} requested removal of productId={} from cart", userId, productId);
        cartService.removeCartItem(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(
            @RequestHeader("X-User-Id") String userId
    ) {
        log.warn("UserId={} requested to clear entire cart", userId);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
