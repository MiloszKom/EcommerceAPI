package com.example.product_service.unit;

import com.example.product_service.dto.ProductDetailsDto;
import com.example.product_service.dto.ProductRequestDto;
import com.example.product_service.dto.ProductSummaryDto;
import com.example.product_service.entity.Product;
import com.example.product_service.exception.ConflictException;
import com.example.product_service.exception.ResourceNotFoundException;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplUnitTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product createTestProduct() {
        Product product = new Product();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(new BigDecimal("19.99"));
        product.setStock(10);
        return product;
    }

    private ProductRequestDto createTestProductRequest() {
        return new ProductRequestDto(
                "Test Product",
                "Test Description",
                new BigDecimal("19.99"),
                10
        );
    }

    @Test
    void getProducts_ShouldReturnAllProducts() {
        // Arrange
        Product product1 = createTestProduct();
        Product product2 = createTestProduct();
        product2.setName("Test Product 2");

        when(repository.findAll()).thenReturn(List.of(product1, product2));

        // Act
        List<ProductSummaryDto> result = productService.getProducts();

        // Assert
        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void getProducts_WhenNoProducts_ShouldReturnEmptyList() {
        // Arrange
        when(repository.findAll()).thenReturn(List.of());

        // Act
        List<ProductSummaryDto> result = productService.getProducts();

        // Assert
        assertTrue(result.isEmpty());
        verify(repository, times(1)).findAll();
    }

    @Test
    void getProductById_WithValidId_ShouldReturnProduct() {
        // Arrange
        Long productId = 1L;
        Product product = createTestProduct();
        when(repository.findById(productId)).thenReturn(Optional.of(product));

        // Act
        ProductDetailsDto result = productService.getProductById(productId);

        // Assert
        assertNotNull(result);
        assertEquals(product.getName(), result.name());
        verify(repository, times(1)).findById(productId);
    }

    @Test
    void getProductById_WithInvalidId_ShouldThrowException() {
        // Arrange
        Long productId = 999L;
        when(repository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> productService.getProductById(productId));
        verify(repository, times(1)).findById(productId);
    }

    @Test
    void createProduct_WithValidRequest_ShouldCreateProduct() {
        // Arrange
        ProductRequestDto request = createTestProductRequest();
        Product savedProduct = createTestProduct();

        when(repository.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        ProductDetailsDto result = productService.createProduct(request);

        // Assert
        assertNotNull(result);
        assertEquals(savedProduct.getId(), result.id());
        assertEquals(request.name(), result.name());
        verify(repository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_WithValidId_ShouldUpdateProduct() {
        // Arrange
        Long productId = 1L;
        ProductRequestDto request = new ProductRequestDto(
                "Updated Product",
                "Updated Description",
                new BigDecimal("29.99"),
                20
        );

        Product existingProduct = createTestProduct();
        Product updatedProduct = createTestProduct();
        updatedProduct.setName("Updated Product");
        updatedProduct.setDescription("Updated Description");
        updatedProduct.setPrice(new BigDecimal("29.99"));
        updatedProduct.setStock(20);

        when(repository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(repository.save(any(Product.class))).thenReturn(updatedProduct);

        // Act
        ProductDetailsDto result = productService.updateProduct(productId, request);

        // Assert
        assertNotNull(result);
        assertEquals(request.name(), result.name());
        assertEquals(request.description(), result.description());
        verify(repository, times(1)).findById(productId);
        verify(repository, times(1)).save(existingProduct);
    }

    @Test
    void updateProduct_WithInvalidId_ShouldThrowException() {
        // Arrange
        Long productId = 999L;
        ProductRequestDto request = createTestProductRequest();
        when(repository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(productId, request));
        verify(repository, times(1)).findById(productId);
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_WithValidId_ShouldDeleteProduct() {
        // Arrange
        Long productId = 1L;
        Product product = createTestProduct();
        when(repository.findById(productId)).thenReturn(Optional.of(product));
        doNothing().when(repository).delete(product);

        // Act
        productService.deleteProduct(productId);

        // Assert
        verify(repository, times(1)).findById(productId);
        verify(repository, times(1)).delete(product);
    }

    @Test
    void deleteProduct_WithInvalidId_ShouldThrowException() {
        // Arrange
        Long productId = 999L;
        when(repository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> productService.deleteProduct(productId));
        verify(repository, times(1)).findById(productId);
        verify(repository, never()).delete(any(Product.class));
    }

    @Test
    void reduceStock_WithSufficientStock_ShouldReduceStock() {
        // Arrange
        Long productId = 1L;
        Integer quantity = 5;
        Product product = createTestProduct();
        product.setStock(10);

        when(repository.findById(productId)).thenReturn(Optional.of(product));
        when(repository.save(any(Product.class))).thenReturn(product);

        // Act
        productService.reduceStock(productId, quantity);

        // Assert
        assertEquals(5, product.getStock());
        verify(repository, times(1)).findById(productId);
        verify(repository, times(1)).save(product);
    }

    @Test
    void reduceStock_WithInsufficientStock_ShouldThrowException() {
        // Arrange
        Long productId = 1L;
        Integer quantity = 15;
        Product product = createTestProduct();
        product.setStock(10);

        when(repository.findById(productId)).thenReturn(Optional.of(product));

        // Act & Assert
        assertThrows(ConflictException.class,
                () -> productService.reduceStock(productId, quantity));
        verify(repository, times(1)).findById(productId);
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void increaseStock_WithValidQuantity_ShouldIncreaseStock() {
        // Arrange
        Long productId = 1L;
        Integer quantity = 5;
        Product product = createTestProduct();
        product.setStock(10);

        when(repository.findById(productId)).thenReturn(Optional.of(product));
        when(repository.save(any(Product.class))).thenReturn(product);

        // Act
        productService.increaseStock(productId, quantity);

        // Assert
        assertEquals(15, product.getStock());
        verify(repository, times(1)).findById(productId);
        verify(repository, times(1)).save(product);
    }
}