package com.example.product_service.integration;

import com.example.product_service.entity.Product;
import com.example.product_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private ProductRepository repository;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        product1 = new Product();
        product1.setName("Laptop");
        product1.setDescription("A great laptop");
        product1.setPrice(BigDecimal.valueOf(1000));
        product1.setStock(5);

        product2 = new Product();
        product2.setName("Phone");
        product2.setDescription("A great phone");
        product2.setPrice(BigDecimal.valueOf(800));
        product2.setStock(10);

        repository.save(product1);
        repository.save(product2);
    }

    @Test
    void getProducts_ShouldReturnAllProducts() throws Exception {
        mockMvc.perform(get("/api/products")
                        .header("X-User-Id", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[1].name").value("Phone"));
    }

    @Test
    void getProductById_ShouldReturnCorrectProduct() throws Exception {
        Long id = product1.getId();

        mockMvc.perform(get("/api/products/{id}", id)
                        .header("X-User-Id", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(1000))
                .andExpect(jsonPath("$.stock").value(5));
    }

    @Test
    void createProduct_ShouldReturnCreatedProduct() throws Exception {
        String json = """
                {
                    "name": "Tablet",
                    "description": "A new tablet",
                    "price": 600,
                    "stock": 15
                }
                """;

        mockMvc.perform(post("/api/products")
                        .header("X-User-Id", "123")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Tablet"))
                .andExpect(jsonPath("$.stock").value(15));
    }

    @Test
    void createProduct_ShouldFail_WhenMissingName() throws Exception {
        String json = """
                {
                    "description": "No name",
                    "price": 500,
                    "stock": 5
                }
                """;

        mockMvc.perform(post("/api/products")
                        .header("X-User-Id", "123")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProduct_ShouldUpdateExistingProduct() throws Exception {
        Long id = product1.getId();
        String json = """
                {
                    "name": "Laptop Pro",
                    "description": "Updated laptop",
                    "price": 1200,
                    "stock": 7
                }
                """;

        mockMvc.perform(put("/api/products/{id}", id)
                        .header("X-User-Id", "123")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop Pro"))
                .andExpect(jsonPath("$.price").value(1200))
                .andExpect(jsonPath("$.stock").value(7));
    }

    @Test
    void updateProduct_ShouldFail_WhenProductNotFound() throws Exception {
        String json = """
                {
                    "name": "Non-existent",
                    "description": "Does not exist",
                    "price": 100,
                    "stock": 1
                }
                """;

        mockMvc.perform(put("/api/products/{id}", 9999)
                        .header("X-User-Id", "123")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_ShouldRemoveProduct() throws Exception {
        Long id = product2.getId();

        mockMvc.perform(delete("/api/products/{id}", id)
                        .header("X-User-Id", "123"))
                .andExpect(status().isNoContent());

        assertEquals(1, repository.count());
    }

    @Test
    void deleteProduct_ShouldFail_WhenNotFound() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", 9999)
                        .header("X-User-Id", "123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void reduceStock_ShouldDecreaseStock() throws Exception {
        Long id = product1.getId();

        String json = """
                {
                    "quantity": 3
                }
                """;

        mockMvc.perform(put("/internal/products/{id}/reduce-stock", id)
                        .header("X-User-Id", "123")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk());

        Product updated = repository.findById(id).orElseThrow();
        assertEquals(2, updated.getStock());
    }

    @Test
    void reduceStock_ShouldFail_WhenInsufficientStock() throws Exception {
        Long id = product1.getId();

        String json = """
                {
                    "quantity": 100
                }
                """;

        mockMvc.perform(put("/internal/products/{id}/reduce-stock", id)
                        .header("X-User-Id", "123")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    void increaseStock_ShouldIncreaseStock() throws Exception {
        Long id = product2.getId();

        String json = """
                {
                    "quantity": 5
                }
                """;

        mockMvc.perform(put("/internal/products/{id}/increase-stock", id)
                        .header("X-User-Id", "123")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk());

        Product updated = repository.findById(id).orElseThrow();
        assertEquals(15, updated.getStock());
    }
}
