package com.example.product_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.product_service.dto.ProductRequest;
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

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class ProductControllerIntegrationTest {

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

    private Long product1Id;
    private Long product2Id;

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

        Product product2 = new Product();
        product2.setName("product2");
        product2.setDescription("description2");
        product2.setCategory("category2");
        product2.setPrice(new BigDecimal("200.00"));
        product2.setStock(20);

        product1Id = productRepository.save(product1).getId();
        product2Id = productRepository.save(product2).getId();
    }

    @Test
    void callingEndpointWithoutInternalSecret_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProducts_returnsAllProducts() throws Exception {
        mockMvc.perform(get("/api/products")
                        .header("X-Internal-Secret", gatewaySecret))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getProducts_withCategoryFilter_returnsOnlyMatchingProducts() throws Exception {
        mockMvc.perform(get("/api/products")
                        .header("X-Internal-Secret", gatewaySecret)
                        .param("category", "category1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("product1"))
                .andExpect(jsonPath("$[0].category").value("category1"));
    }

    @Test
    void getProducts_withMaxPriceFilter_returnsProductsUnderMaxPrice() throws Exception {
        mockMvc.perform(get("/api/products")
                        .header("X-Internal-Secret", gatewaySecret)
                        .param("maxPrice", "200.00"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].price").value(100.00))
                .andExpect(jsonPath("$[1].price").value(200.00));
    }

    @Test
    void createProduct_withValidRequestBody_returnsCreatedProduct() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("New Product Name");
        request.setDescription("New Product Description");
        request.setCategory("New Product Category");
        request.setPrice(new BigDecimal("100.00"));
        request.setStock(10);

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/products")
                        .header("X-Internal-Secret", gatewaySecret)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Product Name"));
    }

    @Test
    void createProduct_withoutValidRequestBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/products")
                        .header("X-Internal-Secret", gatewaySecret)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProductById_existingId_returnsProduct() throws Exception {
        mockMvc.perform(get("/api/products/" + product1Id)
                        .header("X-Internal-Secret", gatewaySecret))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("product1"));
    }

    @Test
    void getProductById_nonExistingId_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/products/999")
                        .header("X-Internal-Secret", gatewaySecret))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProduct_existingId_updatesAndReturnsUpdatedProduct() throws Exception {
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("Updated Product Name");
        updateRequest.setDescription("Updated Description");
        updateRequest.setCategory("Updated Category");
        updateRequest.setPrice(new BigDecimal("150.00"));
        updateRequest.setStock(15);

        String json = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/api/products/" + product1Id)
                        .header("X-Internal-Secret", gatewaySecret)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product1Id))
                .andExpect(jsonPath("$.name").value("Updated Product Name"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.category").value("updated category"))
                .andExpect(jsonPath("$.price").value(150.00))
                .andExpect(jsonPath("$.stock").value(15));

        Product updatedProduct = productRepository.findById(product1Id).orElseThrow();
        assertEquals("Updated Product Name", updatedProduct.getName());
        assertEquals("Updated Description", updatedProduct.getDescription());
        assertEquals("updated category", updatedProduct.getCategory());
        assertEquals(new BigDecimal("150.00"), updatedProduct.getPrice());
        assertEquals(15, updatedProduct.getStock());
    }


    @Test
    void updateProduct_nonExistingId_returnsNotFound() throws Exception {
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("Updated Product Name");
        updateRequest.setDescription("Updated Description");
        updateRequest.setCategory("category1");
        updateRequest.setPrice(new BigDecimal("150.00"));
        updateRequest.setStock(15);

        String json = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/api/products/999")
                        .header("X-Internal-Secret", gatewaySecret)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_existingId_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/products/" + product2Id)
                        .header("X-Internal-Secret", gatewaySecret))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_nonExistingId_returnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/products/999")
                        .header("X-Internal-Secret", gatewaySecret))
                .andExpect(status().isNotFound());
    }
}
