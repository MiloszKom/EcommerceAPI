package com.example.cart_service.unit;

import com.example.cart_service.dto.CartDto;
import com.example.cart_service.dto.CartItemDto;
import com.example.cart_service.dto.CartRequest;
import com.example.cart_service.dto.UpdateCartItemRequest;
import com.example.cart_service.dto.client.ProductDto;
import com.example.cart_service.entity.Cart;
import com.example.cart_service.entity.CartItem;
import com.example.cart_service.exception.ConflictException;
import com.example.cart_service.exception.ExternalServiceException;
import com.example.cart_service.exception.ResourceNotFoundException;
import com.example.cart_service.repository.CartRepository;
import com.example.cart_service.service.client.ProductFeignClient;
import com.example.cart_service.service.impl.CartServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplUnitTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductFeignClient productFeignClient;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart createTestCart(String userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());
        return cart;
    }

    private Cart createTestCartWithItems(String userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);

        CartItem item1 = new CartItem();
        item1.setProductId(101L);
        item1.setQuantity(2);
        item1.setCart(cart);

        CartItem item2 = new CartItem();
        item2.setProductId(102L);
        item2.setQuantity(1);
        item2.setCart(cart);

        List<CartItem> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        cart.setItems(items);

        // Set IDs using reflection since there are no setId methods
        setCartItemId(item1, 1L);
        setCartItemId(item2, 2L);

        return cart;
    }

    // Helper method to set CartItem ID using reflection
    private void setCartItemId(CartItem cartItem, Long id) {
        try {
            Field idField = CartItem.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(cartItem, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set CartItem ID via reflection", e);
        }
    }

    // Helper method to set Cart ID using reflection
    private void setCartId(Cart cart, Long id) {
        try {
            Field idField = Cart.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(cart, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set Cart ID via reflection", e);
        }
    }

    private ProductDto createTestProductDto(Long productId) {
        return new ProductDto(
                productId,
                "Test Product " + productId,
                "Test Description",
                java.math.BigDecimal.valueOf(19.99),
                10
        );
    }

    private CartRequest createTestCartRequest(Long productId, int quantity) {
        return new CartRequest(productId, quantity);
    }

    private UpdateCartItemRequest createTestUpdateRequest(int quantity) {
        return new UpdateCartItemRequest(quantity);
    }

    @Test
    void getUserCart_WithExistingCart_ShouldReturnCart() {
        // Arrange
        String userId = "user123";
        Cart existingCart = createTestCartWithItems(userId);
        setCartId(existingCart, 1L); // Set cart ID
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));

        // Act
        CartDto result = cartService.getUserCart(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.userId());
        assertEquals(2, result.items().size());
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void getUserCart_WithNewUser_ShouldCreateNewCart() {
        // Arrange
        String userId = "newUser";
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            cart.setUserId(userId);
            setCartId(cart, 1L); // Simulate ID generation
            return cart;
        });

        // Act
        CartDto result = cartService.getUserCart(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.userId());
        assertTrue(result.items().isEmpty());
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void addProductToCart_WithNewProduct_ShouldAddProduct() {
        // Arrange
        String userId = "user123";
        Long productId = 103L;
        int quantity = 3;

        CartRequest request = createTestCartRequest(productId, quantity);
        Cart existingCart = createTestCart(userId);
        setCartId(existingCart, 1L);
        ProductDto productDto = createTestProductDto(productId);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));
        when(productFeignClient.getProductById(productId)).thenReturn(productDto);
        when(cartRepository.save(any(Cart.class))).thenReturn(existingCart);

        // Act
        CartDto result = cartService.addProductToCart(userId, request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.items().size());
        assertEquals(productId, result.items().get(0).productId());
        assertEquals(quantity, result.items().get(0).quantity());

        verify(cartRepository, times(1)).findByUserId(userId);
        verify(productFeignClient, times(1)).getProductById(productId);
        verify(cartRepository, times(1)).save(existingCart);
    }

    @Test
    void addProductToCart_WithExistingProduct_ShouldThrowConflictException() {
        // Arrange
        String userId = "user123";
        Long existingProductId = 101L;
        CartRequest request = createTestCartRequest(existingProductId, 2);
        Cart existingCart = createTestCartWithItems(userId);
        setCartId(existingCart, 1L);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));

        // Act & Assert
        assertThrows(ConflictException.class,
                () -> cartService.addProductToCart(userId, request));

        verify(cartRepository, times(1)).findByUserId(userId);
        verify(productFeignClient, never()).getProductById(anyLong());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void addProductToCart_WithNonExistentProduct_ShouldThrowExternalServiceException() {
        // Arrange
        String userId = "user123";
        Long nonExistentProductId = 999L;
        CartRequest request = createTestCartRequest(nonExistentProductId, 1);
        Cart existingCart = createTestCart(userId);
        setCartId(existingCart, 1L);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));
        when(productFeignClient.getProductById(nonExistentProductId))
                .thenThrow(new ExternalServiceException("Product not found", HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThrows(ExternalServiceException.class,
                () -> cartService.addProductToCart(userId, request));

        verify(cartRepository, times(1)).findByUserId(userId);
        verify(productFeignClient, times(1)).getProductById(nonExistentProductId);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void updateCartItem_WithValidItem_ShouldUpdateQuantity() {
        // Arrange
        String userId = "user123";
        Long itemId = 1L;
        int newQuantity = 5;

        UpdateCartItemRequest request = createTestUpdateRequest(newQuantity);
        Cart existingCart = createTestCartWithItems(userId);
        setCartId(existingCart, 1L);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(existingCart);

        // Act
        CartDto result = cartService.updateCartItem(userId, itemId, request);

        // Assert
        assertNotNull(result);
        // Verify the quantity was updated for the specific item
        Optional<CartItemDto> updatedItem = result.items().stream()
                .filter(item -> item.itemId().equals(itemId))
                .findFirst();
        assertTrue(updatedItem.isPresent());
        assertEquals(newQuantity, updatedItem.get().quantity());

        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, times(1)).save(existingCart);
    }

    @Test
    void updateCartItem_WithInvalidItemId_ShouldThrowResourceNotFoundException() {
        // Arrange
        String userId = "user123";
        Long invalidItemId = 999L;
        UpdateCartItemRequest request = createTestUpdateRequest(3);
        Cart existingCart = createTestCartWithItems(userId);
        setCartId(existingCart, 1L);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> cartService.updateCartItem(userId, invalidItemId, request));

        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void removeCartItem_WithValidItem_ShouldRemoveItem() {
        // Arrange
        String userId = "user123";
        Long itemIdToRemove = 1L;
        Cart existingCart = createTestCartWithItems(userId);
        setCartId(existingCart, 1L);
        int initialItemCount = existingCart.getItems().size();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(existingCart);

        // Act
        CartDto result = cartService.removeCartItem(userId, itemIdToRemove);

        // Assert
        assertNotNull(result);
        assertEquals(initialItemCount - 1, result.items().size());
        assertTrue(result.items().stream().noneMatch(item -> item.itemId().equals(itemIdToRemove)));

        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, times(1)).save(existingCart);
    }

    @Test
    void removeCartItem_WithInvalidItemId_ShouldThrowResourceNotFoundException() {
        // Arrange
        String userId = "user123";
        Long invalidItemId = 999L;
        Cart existingCart = createTestCartWithItems(userId);
        setCartId(existingCart, 1L);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> cartService.removeCartItem(userId, invalidItemId));

        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void clearCart_ShouldRemoveAllItems() {
        // Arrange
        String userId = "user123";
        Cart existingCart = createTestCartWithItems(userId);
        setCartId(existingCart, 1L);
        int initialItemCount = existingCart.getItems().size();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(existingCart);

        // Act
        CartDto result = cartService.clearCart(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.items().isEmpty());

        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, times(1)).save(existingCart);
    }

    @Test
    void clearCart_WithEmptyCart_ShouldReturnEmptyCart() {
        // Arrange
        String userId = "user123";
        Cart emptyCart = createTestCart(userId);
        setCartId(emptyCart, 1L);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(emptyCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(emptyCart);

        // Act
        CartDto result = cartService.clearCart(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.items().isEmpty());

        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, times(1)).save(emptyCart);
    }
}
