package com.example.EcommerceAPI.cart;

import com.example.EcommerceAPI.cart.dto.AddToCartRequest;
import com.example.EcommerceAPI.cart.dto.CartDetailsDTO;
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
    public ResponseEntity<CartDetailsDTO> addProductToCart(@Valid @RequestBody AddToCartRequest cartItemDTO) {
        CartDetailsDTO updatedCart = service.addProduct(cartItemDTO);
        return ResponseEntity.ok(updatedCart);
    }

    @PutMapping
    public ResponseEntity<CartDetailsDTO> updateCartItem(@Valid @RequestBody AddToCartRequest cartItemDTO) {
        CartDetailsDTO updatedCart = service.updateQuantity(cartItemDTO);
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
