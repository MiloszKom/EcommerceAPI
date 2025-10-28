package com.example.product_service.integration;


import com.example.product_service.dto.ProductRequestDto;
import com.example.product_service.entity.Product;
import com.example.product_service.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ProductControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Product existingProduct;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("KEYCLOAK_BASE_URL", () -> "http://localhost:8080");
        registry.add("API_GATEWAY_URL", () -> "http://localhost:8080");
    }

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        // Create a test product
        existingProduct = new Product();
        existingProduct.setName("Test Product");
        existingProduct.setDescription("Test Description");
        existingProduct.setPrice(new BigDecimal("99.99"));
        existingProduct.setStock(100);
        productRepository.save(existingProduct);
    }

    @Test
    void getProducts_WithValidJwt_ReturnsAllProducts() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j
                                .claim("sub", "test-user-id")
                                .claim("preferred_username", "testuser")
                                .claim("email", "test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$[0].price").value(99.99));
    }

    @Test
    void getProducts_WithoutJwt_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProductById_WithValidJwt_ReturnsProductDetails() throws Exception {
        // Arrange
        Long productId = existingProduct.getId();

        // Act & Assert
        mockMvc.perform(get("/api/products/{id}", productId)
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j
                                .claim("sub", "test-user-id")
                                .claim("preferred_username", "testuser")
                                .claim("email", "test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.stock").value(100));
    }

    @Test
    void getProductById_WithInvalidId_ReturnsNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products/{id}", 999L)
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j
                                .claim("sub", "test-user-id")
                                .claim("preferred_username", "testuser")
                                .claim("email", "test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProduct_AsAdmin_ReturnsCreatedProduct() throws Exception {
        // Arrange
        ProductRequestDto requestDto = new ProductRequestDto(
                "New Product",
                "New Description",
                new BigDecimal("149.99"),
                50
        );

        // Act & Assert
        mockMvc.perform(post("/api/products")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("New Product"))
                .andExpect(jsonPath("$.description").value("New Description"))
                .andExpect(jsonPath("$.price").value(149.99))
                .andExpect(jsonPath("$.stock").value(50));

        List<Product> products = productRepository.findAll();
        assertEquals(2, products.size());
        Product created = products.stream()
                .filter(p -> p.getName().equals("New Product"))
                .findFirst()
                .orElseThrow();
        assertEquals("New Description", created.getDescription());
        assertEquals(new BigDecimal("149.99"), created.getPrice());
        assertEquals(50, created.getStock());
    }

    @Test
    void createProduct_AsUser_ReturnsForbidden() throws Exception {
        // Arrange
        ProductRequestDto requestDto = new ProductRequestDto(
                "New Product",
                "New Description",
                new BigDecimal("199.99"),
                75
        );

        // Act & Assert
        mockMvc.perform(post("/api/products")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j
                                .claim("sub", "test-user-id")
                                .claim("preferred_username", "testuser")
                                .claim("email", "test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProduct_WithoutJwt_ReturnsUnauthorized() throws Exception {
        // Arrange
        ProductRequestDto requestDto = new ProductRequestDto(
                "New Product",
                "New Description",
                new BigDecimal("199.99"),
                75
        );

        // Act & Assert
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProduct_WithValidJwtAndData_ReturnsUpdatedProduct() throws Exception {
        // Arrange
        Long productId = existingProduct.getId();
        ProductRequestDto updateDto = new ProductRequestDto(
                "Updated Product",
                "Updated Description",
                new BigDecimal("199.99"),
                75
        );

        // Act & Assert
        mockMvc.perform(put("/api/products/{id}", productId)
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j
                                .claim("sub", "test-user-id")
                                .claim("preferred_username", "testuser")
                                .claim("email", "test@example.com")))
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.price").value(199.99))
                .andExpect(jsonPath("$.stock").value(75));

        Product updated = productRepository.findById(productId).orElseThrow();
        assertEquals("Updated Product", updated.getName());
        assertEquals("Updated Description", updated.getDescription());
        assertEquals(new BigDecimal("199.99"), updated.getPrice());
        assertEquals(75, updated.getStock());
    }

    @Test
    void updateProduct_WithInvalidId_ReturnsNotFound() throws Exception {
        // Arrange
        ProductRequestDto updateDto = new ProductRequestDto(
                "Updated Product",
                "Updated Description",
                new BigDecimal("199.99"),
                75
        );

        // Act & Assert
        mockMvc.perform(put("/api/products/{id}", 999L)
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j
                                .claim("sub", "test-user-id")
                                .claim("preferred_username", "testuser")
                                .claim("email", "test@example.com")))
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProduct_WithoutJwt_ReturnsUnauthorized() throws Exception {
        // Arrange
        ProductRequestDto updateDto = new ProductRequestDto(
                "Updated Product",
                "Updated Description",
                new BigDecimal("199.99"),
                75
        );

        // Act & Assert
        mockMvc.perform(put("/api/products/{id}", existingProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteProduct_AsAdmin_WithValidJwt_DeletesProduct() throws Exception {
        // Arrange
        Long productId = existingProduct.getId();

        // Act & Assert
        mockMvc.perform(delete("/api/products/{id}", productId)
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j
                                .claim("sub", "test-user-id")
                                .claim("preferred_username", "testuser")
                                .claim("email", "test@example.com")))
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertFalse(productRepository.existsById(productId));
    }

    @Test
    void deleteProduct_AsAdmin_WithInvalidId_ReturnsNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/products/{id}", 999L)
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j
                                .claim("sub", "test-user-id")
                                .claim("preferred_username", "testuser")
                                .claim("email", "test@example.com")))
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_WithoutJwt_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/products/{id}", existingProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
