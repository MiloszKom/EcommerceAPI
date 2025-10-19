package com.example.product_service.unit;

import com.example.product_service.dto.ProductDetailsDto;
import com.example.product_service.dto.ProductRequestDto;
import com.example.product_service.dto.ProductSummaryDto;
import com.example.product_service.entity.Product;
import com.example.product_service.exception.ConflictException;
import com.example.product_service.exception.ResourceNotFoundException;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ProductServiceImplUnitTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getProducts_ShouldMapAllProductsCorrectly() {
        Product p1 = createProduct(1L, "Laptop", "A great laptop", BigDecimal.valueOf(1000), 5);
        Product p2 = createProduct(2L, "Phone", "A great phone", BigDecimal.valueOf(800), 10);

        when(repository.findAll()).thenReturn(List.of(p1, p2));

        List<ProductSummaryDto> result = service.getProducts();

        assertEquals(2, result.size());
        assertEquals("Laptop", result.get(0).name());
        assertEquals("Phone", result.get(1).name());
        verify(repository, times(1)).findAll();
    }

    @Test
    void getProductById_ShouldReturnProductDetailsDto_WhenProductExists() {
        Product product = createProduct(1L, "Laptop", "A great laptop", BigDecimal.valueOf(1000), 5);
        when(repository.findById(1L)).thenReturn(Optional.of(product));

        ProductDetailsDto result = service.getProductById(1L);

        assertEquals("Laptop", result.name());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void getProductById_ShouldThrowException_WhenProductDoesNotExist() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getProductById(999L));
    }

    @Test
    void createProduct_ShouldSaveAndReturnCreatedProduct() {
        ProductRequestDto request = new ProductRequestDto("Laptop", "Nice laptop", BigDecimal.valueOf(1000), 5);
        Product saved = createProduct(1L, "Laptop", "Nice laptop", BigDecimal.valueOf(1000), 5);

        when(repository.save(any(Product.class))).thenReturn(saved);

        ProductDetailsDto result = service.createProduct(request);

        assertEquals("Laptop", result.name());
        assertEquals(5, result.stock());
        verify(repository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_ShouldUpdateAndReturnUpdatedProduct() {
        Product existing = createProduct(1L, "Old Laptop", "Old desc", BigDecimal.valueOf(800), 10);
        Product updated = createProduct(1L, "New Laptop", "New desc", BigDecimal.valueOf(1200), 8);
        ProductRequestDto request = new ProductRequestDto("New Laptop", "New desc", BigDecimal.valueOf(1200), 8);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(updated);

        ProductDetailsDto result = service.updateProduct(1L, request);

        assertEquals("New Laptop", result.name());
        assertEquals(8, result.stock());
        verify(repository, times(1)).save(existing);
    }

    @Test
    void updateProduct_ShouldThrowException_WhenProductDoesNotExist() {
        ProductRequestDto request = new ProductRequestDto("New Laptop", "New desc", BigDecimal.valueOf(1200), 8);
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.updateProduct(1L, request));
    }

    @Test
    void deleteProduct_ShouldDeleteExistingProduct() {
        Product product = createProduct(1L, "Laptop", "Nice laptop", BigDecimal.valueOf(1000), 5);
        when(repository.findById(1L)).thenReturn(Optional.of(product));

        service.deleteProduct(1L);

        verify(repository, times(1)).delete(product);
    }

    @Test
    void deleteProduct_ShouldThrowException_WhenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.deleteProduct(1L));
    }

    @Test
    void reduceStock_ShouldReduceQuantityAndSave() {
        Product product = createProduct(1L, "Laptop", "desc", BigDecimal.valueOf(1000), 10);
        when(repository.findById(1L)).thenReturn(Optional.of(product));

        service.reduceStock(1L, 3);

        assertEquals(7, product.getStock());
        verify(repository).save(product);
    }

    @Test
    void reduceStock_ShouldThrowConflictException_WhenStockInsufficient() {
        Product product = createProduct(1L, "Laptop", "desc", BigDecimal.valueOf(1000), 2);
        when(repository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(ConflictException.class, () -> service.reduceStock(1L, 5));
    }

    @Test
    void increaseStock_ShouldIncreaseQuantityAndSave() {
        Product product = createProduct(1L, "Laptop", "desc", BigDecimal.valueOf(1000), 5);
        when(repository.findById(1L)).thenReturn(Optional.of(product));

        service.increaseStock(1L, 3);

        assertEquals(8, product.getStock());
        verify(repository).save(product);
    }

    // --- Helper method ---
    private static Product createProduct(Long id, String name, String description, BigDecimal price, int stock) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);

        if (id != null) {
            try {
                Field idField = Product.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(product, id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return product;
    }
}
