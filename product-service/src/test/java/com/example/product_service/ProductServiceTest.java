package com.example.product_service;

import com.example.product_service.exception.types.ConflictException;
import com.example.product_service.exception.types.ProductNotFoundException;
import com.example.product_service.model.Product;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.service.ProductService;
import com.example.product_service.dto.ProductDetailsDTO;
import com.example.product_service.dto.ProductRequest;
import com.example.product_service.dto.ProductSummaryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    private ProductService underTest;

    @BeforeEach
    void setUp() {
        underTest = new ProductService(repository);
    }

    // --- Helper for creating products in tests ---
    private static Product createProduct(Long id, String name, String category, BigDecimal price, int stock) {
        Product product = new Product();
        product.setName(name);
        product.setDescription("Description for " + name);
        product.setCategory(category);
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

    @Test
    void getProduct_whenExists_returnsProduct() {
        Product product = createProduct(1L, "Yamaha Acoustic Guitar", "guitars", BigDecimal.valueOf(299.99), 15);

        when(repository.findById(1L)).thenReturn(Optional.of(product));

        Product result = underTest.getProduct(1L);

        assertThat(result).isEqualTo(product);
    }

    @Test
    void getProduct_whenNotExists_throwsException() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.getProduct(1L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void createProduct_withValidRequest_savesAndReturnsDto() {
        ProductRequest request = new ProductRequest();
        request.setName("Roland Digital Piano");
        request.setDescription("88-key weighted digital piano with realistic sound and feel.");
        request.setCategory("pianos");
        request.setPrice(BigDecimal.valueOf(899.00));
        request.setStock(8);

        Product saved = createProduct(1L, "Roland Digital Piano", "pianos", BigDecimal.valueOf(899.00), 8);

        when(repository.save(any(Product.class))).thenReturn(saved);

        ProductDetailsDTO result = underTest.createProduct(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Roland Digital Piano");
        assertThat(result.category()).isEqualTo("pianos");

        verify(repository).save(any(Product.class));
    }

    @Test
    void getProducts_returnsListOfSummaryDtos() {
        Product product1 = createProduct(1L, "Fender Stratocaster", "guitars", BigDecimal.valueOf(1200.00), 5);
        Product product2 = createProduct(2L, "Pearl Drum Kit", "drums", BigDecimal.valueOf(1500.00), 3);

        when(repository.findProducts("guitars", BigDecimal.valueOf(2000.00)))
                .thenReturn(List.of(product1));

        List<ProductSummaryDTO> result = underTest.getProducts("guitars", BigDecimal.valueOf(2000.00));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Fender Stratocaster");
        assertThat(result.get(0).category()).isEqualTo("guitars");
    }

    @Test
    void updateProduct_whenExists_updatesFields() {
        Product product = createProduct(1L, "Old Violin", "strings", BigDecimal.valueOf(199.00), 12);

        ProductRequest request = new ProductRequest();
        request.setName("Yamaha Violin");
        request.setDescription("Professional violin with premium sound quality.");
        request.setCategory("strings");
        request.setPrice(BigDecimal.valueOf(499.00));
        request.setStock(8);

        when(repository.findById(1L)).thenReturn(Optional.of(product));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductDetailsDTO result = underTest.updateProduct(1L, request);

        assertThat(result.name()).isEqualTo("Yamaha Violin");
        assertThat(result.category()).isEqualTo("strings");
        verify(repository).save(product);
    }

    @Test
    void deleteProduct_whenExists_deletesFromRepo() {
        Product product = createProduct(1L, "Korg Synthesizer", "keyboards", BigDecimal.valueOf(650.00), 7);

        when(repository.findById(1L)).thenReturn(Optional.of(product));

        underTest.deleteProduct(1L);

        verify(repository).delete(product);
    }

    @Test
    void reduceStock_whenEnoughStock_reducesAndSaves() {
        Product product = createProduct(1L, "Ibanez Bass Guitar", "guitars", BigDecimal.valueOf(799.00), 10);

        when(repository.findById(1L)).thenReturn(Optional.of(product));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = underTest.reduceStock(1L, 4);

        assertThat(result.getStock()).isEqualTo(6);
    }

    @Test
    void reduceStock_whenNotEnoughStock_throwsConflictException() {
        Product product = createProduct(1L, "Yamaha Drum Set", "drums", BigDecimal.valueOf(599.00), 2);

        when(repository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> underTest.reduceStock(1L, 5))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void increaseStock_increasesAndSaves() {
        Product product = createProduct(1L, "Casio Keyboard", "keyboards", BigDecimal.valueOf(299.00), 20);

        when(repository.findById(1L)).thenReturn(Optional.of(product));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = underTest.increaseStock(1L, 5);

        assertThat(result.getStock()).isEqualTo(25);
    }
}
