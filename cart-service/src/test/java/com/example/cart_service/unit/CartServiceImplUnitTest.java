package com.example.cart_service.unit;

import com.example.cart_service.dto.CartDto;
import com.example.cart_service.dto.CartRequest;
import com.example.cart_service.dto.client.ProductDto;
import com.example.cart_service.entity.Cart;
import com.example.cart_service.entity.CartItem;
import com.example.cart_service.exception.ConflictException;
import com.example.cart_service.exception.ResourceNotFoundException;
import com.example.cart_service.repository.CartRepository;
import com.example.cart_service.service.client.ProductFeignClient;
import com.example.cart_service.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CartServiceImplUnitTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductFeignClient productFeignClient;

    @InjectMocks
    private CartServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- getUserCart ---
    @Test
    void getUserCart_ShouldReturnExistingCart() {
        Cart cart = createCart("user1", List.of(createCartItem(1L, 2)));
        when(cartRepository.findByUserId("user1")).thenReturn(Optional.of(cart));

        CartDto result = service.getUserCart("user1");

        assertEquals("user1", result.userId());
        verify(cartRepository, times(1)).findByUserId("user1");
    }

    @Test
    void getUserCart_ShouldCreateNewCart_WhenNoneExists() {
        when(cartRepository.findByUserId("newUser")).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        CartDto result = service.getUserCart("newUser");

        assertEquals("newUser", result.userId());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    // --- addProductToCart ---
    @Test
    void addProductToCart_ShouldAddProduct_WhenNotPresent() {
        Cart cart = createCart("user1", new ArrayList<>());
        when(cartRepository.findByUserId("user1")).thenReturn(Optional.of(cart));
        when(productFeignClient.getProductById(1L))
                .thenReturn(new ProductDto(1L, "Laptop", "desc", null, 5));
        when(cartRepository.save(cart)).thenReturn(cart);

        CartRequest request = new CartRequest(1L, 2);
        CartDto result = service.addProductToCart("user1", request);

        assertEquals(1, result.items().size());
        verify(cartRepository).save(cart);
    }

    @Test
    void addProductToCart_ShouldThrowConflict_WhenProductAlreadyExists() {
        CartItem existing = createCartItem(1L, 2);
        Cart cart = createCart("user1", List.of(existing));
        when(cartRepository.findByUserId("user1")).thenReturn(Optional.of(cart));

        CartRequest request = new CartRequest(1L, 1);
        assertThrows(ConflictException.class, () -> service.addProductToCart("user1", request));
    }

    // --- updateCartItem ---
    @Test
    void updateCartItem_ShouldUpdateQuantity_WhenProductExists() {
        CartItem existing = createCartItem(1L, 2);
        Cart cart = createCart("user1", new ArrayList<>(List.of(existing)));
        when(cartRepository.findByUserId("user1")).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);

        CartRequest request = new CartRequest(1L, 5);
        CartDto result = service.updateCartItem("user1", request);

        assertEquals(5, result.items().get(0).quantity());
        verify(cartRepository).save(cart);
    }

    @Test
    void updateCartItem_ShouldThrowNotFound_WhenItemMissing() {
        Cart cart = createCart("user1", new ArrayList<>());
        when(cartRepository.findByUserId("user1")).thenReturn(Optional.of(cart));

        CartRequest request = new CartRequest(99L, 2);
        assertThrows(ResourceNotFoundException.class, () -> service.updateCartItem("user1", request));
    }

    // --- removeCartItem ---
    @Test
    void removeCartItem_ShouldRemoveItem_WhenExists() {
        CartItem existing = createCartItem(1L, 2);
        Cart cart = createCart("user1", new ArrayList<>(List.of(existing)));
        when(cartRepository.findByUserId("user1")).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);

        CartDto result = service.removeCartItem("user1", 1L);

        assertTrue(result.items().isEmpty());
        verify(cartRepository).save(cart);
    }

    @Test
    void removeCartItem_ShouldThrowNotFound_WhenItemMissing() {
        Cart cart = createCart("user1", new ArrayList<>());
        when(cartRepository.findByUserId("user1")).thenReturn(Optional.of(cart));

        assertThrows(ResourceNotFoundException.class, () -> service.removeCartItem("user1", 1L));
    }

    // --- clearCart ---
    @Test
    void clearCart_ShouldRemoveAllItems() {
        CartItem item1 = createCartItem(1L, 2);
        CartItem item2 = createCartItem(2L, 3);
        Cart cart = createCart("user1", new ArrayList<>(List.of(item1, item2)));
        when(cartRepository.findByUserId("user1")).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);

        CartDto result = service.clearCart("user1");

        assertEquals(0, result.items().size());
        verify(cartRepository).save(cart);
    }

    // --- Helper methods ---
    private static Cart createCart(String userId, List<CartItem> items) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setItems(items);
        return cart;
    }

    private static CartItem createCartItem(Long productId, int quantity) {
        CartItem item = new CartItem();
        item.setProductId(productId);
        item.setQuantity(quantity);
        return item;
    }
}
