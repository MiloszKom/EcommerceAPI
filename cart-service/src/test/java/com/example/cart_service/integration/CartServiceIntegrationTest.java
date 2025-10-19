package com.example.cart_service.integration;

import com.example.cart_service.dto.CartDto;
import com.example.cart_service.dto.CartItemDto;
import com.example.cart_service.dto.CartRequest;
import com.example.cart_service.dto.client.ProductDto;
import com.example.cart_service.repository.CartRepository;
import com.example.cart_service.service.ICartService;
import com.example.cart_service.service.client.ProductFeignClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class CartServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ICartService cartService;

    @MockBean
    private ProductFeignClient productFeignClient;

    private final String userId = "user1";

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();
    }

    @Test
    void getUserCart_ShouldCreateCartIfNotExist() {
        CartDto cartDto = cartService.getUserCart(userId);

        assertEquals(userId, cartDto.userId());
        assertEquals(0, cartDto.items().size());
    }

    @Test
    void addProductToCart_ShouldAddItem() {
        ProductDto product = new ProductDto(1L, "Laptop", "desc", BigDecimal.valueOf(1000), 10);
        when(productFeignClient.getProductById(1L)).thenReturn(product);

        CartRequest request = new CartRequest(1L, 2);

        CartDto cartDto = cartService.addProductToCart(userId, request);

        assertEquals(1, cartDto.items().size());
        CartItemDto item = cartDto.items().get(0);
        assertEquals(1L, item.productId());
        assertEquals(2, item.quantity());
    }

    @Test
    void addProductToCart_ShouldFail_WhenAlreadyInCart() {
        ProductDto product = new ProductDto(1L, "Laptop", "desc", BigDecimal.valueOf(1000), 10);
        when(productFeignClient.getProductById(1L)).thenReturn(product);

        CartRequest request = new CartRequest(1L, 2);
        cartService.addProductToCart(userId, request);

        try {
            cartService.addProductToCart(userId, request);
        } catch (Exception e) {
            assertEquals("Product already in cart", e.getMessage());
        }
    }

    @Test
    void updateCartItem_ShouldUpdateQuantity() {
        ProductDto product = new ProductDto(1L, "Laptop", "desc", BigDecimal.valueOf(1000), 10);
        when(productFeignClient.getProductById(1L)).thenReturn(product);

        CartRequest request = new CartRequest(1L, 2);
        cartService.addProductToCart(userId, request);

        CartRequest updateRequest = new CartRequest(1L, 5);
        CartDto updatedCart = cartService.updateCartItem(userId, updateRequest);

        assertEquals(1, updatedCart.items().size());
        assertEquals(5, updatedCart.items().get(0).quantity());
    }

    @Test
    void removeCartItem_ShouldRemoveItem() {
        ProductDto product = new ProductDto(1L, "Laptop", "desc", BigDecimal.valueOf(1000), 10);
        when(productFeignClient.getProductById(1L)).thenReturn(product);

        CartRequest request = new CartRequest(1L, 2);
        cartService.addProductToCart(userId, request);

        CartDto updatedCart = cartService.removeCartItem(userId, 1L);

        assertEquals(0, updatedCart.items().size());
    }

    @Test
    void clearCart_ShouldRemoveAllItems() {
        ProductDto product1 = new ProductDto(1L, "Laptop", "desc", BigDecimal.valueOf(1000), 10);
        ProductDto product2 = new ProductDto(2L, "Phone", "desc", BigDecimal.valueOf(500), 5);
        when(productFeignClient.getProductById(1L)).thenReturn(product1);
        when(productFeignClient.getProductById(2L)).thenReturn(product2);

        cartService.addProductToCart(userId, new CartRequest(1L, 2));
        cartService.addProductToCart(userId, new CartRequest(2L, 1));

        CartDto clearedCart = cartService.clearCart(userId);

        assertEquals(0, clearedCart.items().size());
    }
}