package com.example.product_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.product_service.dto.StockUpdateRequest;
import com.example.product_service.model.Product;
import com.example.product_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class InternalProductControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Value("${app.gateway.secret}")
    private String gatewaySecret;

    private Long productId;

    @BeforeEach
    void setup() {
        productRepository.deleteAll();
        productRepository.flush();

        Product product1 = new Product();
        product1.setName("product1");
        product1.setDescription("description1");
        product1.setCategory("category1");
        product1.setPrice(new BigDecimal("100.00"));
        product1.setStock(10);

        productId = productRepository.save(product1).getId();
    }

    @Test
    void reduceStock_withValidAmount_reducesStockSuccessfully() throws Exception {
        StockUpdateRequest request = new StockUpdateRequest(5);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/internal/products/" + productId + "/reduce-stock")
                        .header("X-Internal-Secret", gatewaySecret)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        Product updated = productRepository.findById(productId).orElseThrow();
        assertEquals(5, updated.getStock());
    }

    @Test
    void reduceStock_withTooHighAmount_returnsConflict() throws Exception {
        StockUpdateRequest request = new StockUpdateRequest(20);

        mockMvc.perform(put("/internal/products/" + productId + "/reduce-stock")
                        .header("X-Internal-Secret", gatewaySecret)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        Product unchanged = productRepository.findById(productId).orElseThrow();
        assertEquals(10, unchanged.getStock());
    }

    @Test
    void reduceStock_nonExistingProduct_returnsNotFound() throws Exception {
        StockUpdateRequest request = new StockUpdateRequest(5);

        mockMvc.perform(put("/internal/products/99999/reduce-stock")
                        .header("X-Internal-Secret", gatewaySecret)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void increaseStock_withValidAmount_increasesStockSuccessfully() throws Exception {
        StockUpdateRequest request = new StockUpdateRequest(5);

        mockMvc.perform(put("/internal/products/" + productId + "/increase-stock")
                        .header("X-Internal-Secret", gatewaySecret)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Product updated = productRepository.findById(productId).orElseThrow();
        assertEquals(15, updated.getStock());
    }

    @Test
    void increaseStock_nonExistingProduct_returnsNotFound() throws Exception {
        StockUpdateRequest request = new StockUpdateRequest(5);

        mockMvc.perform(put("/internal/products/99999/increase-stock")
                        .header("X-Internal-Secret", gatewaySecret)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
