package com.example.cart_service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.cart_service.client.ProductClient;
import com.example.cart_service.dto.AddToCartRequest;
import com.example.cart_service.dto.ProductDTO;
import com.example.cart_service.exception.types.RemoteServiceException;
import com.example.cart_service.model.Cart;
import com.example.cart_service.model.CartItem;
import com.example.cart_service.repository.CartRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class CartControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CartRepository cartRepository;

    @MockBean
    private ProductClient productClient;

    @Value("${app.gateway.secret}")
    private String gatewaySecret;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();
    }

    @Test
    void callingEndpointWithoutInternalSecret_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetUserCart_emptyCart() throws Exception {
        mockMvc.perform(get("/api/cart")
                    .header("X-Internal-Secret", gatewaySecret)
                    .header("userId", 1)
                    .header("role", "ROLE_USER"))
                .andExpect(status().isOk());
    }

    @Test
    void addProductToCart_shouldReturnCartWithProduct_whenProductExists() throws Exception {
        Mockito.when(productClient.getProductById(100L))
                .thenReturn(new ProductDTO(100L, "Name", "Description", "category", new BigDecimal("100.00"), 10));

        AddToCartRequest request = new AddToCartRequest(100L, 2);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/cart/add")
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", 1)
                        .header("role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value(100L))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void addProductToCart_shouldReturn503_whenProductServiceUnavailable() throws Exception {
        Mockito.when(productClient.getProductById(100L))
                .thenThrow(new RemoteServiceException("product-service",
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Product service is unavailable"));

        AddToCartRequest request = new AddToCartRequest(100L, 2);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/cart/add")
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", 1)
                        .header("role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void addProductToCart_shouldReturn409_whenProductAlreadyInCart() throws Exception {
        Mockito.when(productClient.getProductById(100L))
                .thenReturn(new ProductDTO(100L, "Name", "Description", "category", new BigDecimal("100.00"), 10));

        Cart cart = new Cart();
        cart.setUserId(1L);

        CartItem cartItem = new CartItem();
        cartItem.setProductId(100L);
        cartItem.setQuantity(5);
        cart.addItem(cartItem);

        cartRepository.save(cart);

        AddToCartRequest request = new AddToCartRequest(100L, 2);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/cart/add")
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", 1L)
                        .header("role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    void addProductToCart_shouldReturn400_whenInvalidRequest() throws Exception {
        AddToCartRequest request = new AddToCartRequest(null, 0);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/cart/add")
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", 1)
                        .header("role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCartItem_shouldUpdateQuantity_whenProductExistsInCart() throws Exception {
        Cart cart = new Cart();
        cart.setUserId(1L);
        CartItem item = new CartItem();
        item.setProductId(100L);
        item.setQuantity(2);
        cart.addItem(item);
        cartRepository.save(cart);

        AddToCartRequest updateRequest = new AddToCartRequest(100L, 5);
        String json = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/api/cart")
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", 1)
                        .header("role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value(100L))
                .andExpect(jsonPath("$.items[0].quantity").value(5));
    }

    @Test
    void updateCartItem_shouldReturn404_whenProductNotInCart() throws Exception {
        AddToCartRequest updateRequest = new AddToCartRequest(999L, 3);
        String json = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/api/cart")
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", 1)
                        .header("role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void removeCartItem_shouldReturn204_whenProductRemoved() throws Exception {
        Cart cart = new Cart();
        cart.setUserId(1L);
        CartItem item = new CartItem();
        item.setProductId(100L);
        item.setQuantity(2);
        cart.addItem(item);
        cartRepository.save(cart);

        mockMvc.perform(delete("/api/cart/100")
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", 1)
                        .header("role", "ROLE_USER"))
                .andExpect(status().isNoContent());

        Cart updatedCart = cartRepository.findByUserId(1L).orElseThrow();
        assertTrue(updatedCart.getItems().isEmpty());
    }

    @Test
    void removeCartItem_shouldReturn404_whenProductNotInCart() throws Exception {
        mockMvc.perform(delete("/api/cart/999")
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", 1)
                        .header("role", "ROLE_USER"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void clearCart_shouldReturn204_andEmptyCart() throws Exception {
        Cart cart = new Cart();
        cart.setUserId(1L);
        CartItem item1 = new CartItem();
        item1.setProductId(100L);
        item1.setQuantity(1);
        cart.addItem(item1);

        CartItem item2 = new CartItem();
        item2.setProductId(200L);
        item2.setQuantity(2);
        cart.addItem(item2);
        cartRepository.save(cart);

        mockMvc.perform(delete("/api/cart/clear")
                        .header("X-Internal-Secret", gatewaySecret)
                        .header("userId", 1)
                        .header("role", "ROLE_USER"))
                .andExpect(status().isNoContent());

        Cart updatedCart = cartRepository.findByUserId(1L).orElseThrow();
        assertTrue(updatedCart.getItems().isEmpty());
    }

}
