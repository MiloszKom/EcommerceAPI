package com.example.product_service.service;

import com.example.product_service.dto.ProductDetailsDTO;
import com.example.product_service.dto.ProductRequest;
import com.example.product_service.dto.ProductSummaryDTO;
import com.example.product_service.exception.types.ConflictException;
import com.example.product_service.exception.types.ProductNotFoundException;
import com.example.product_service.mapper.ProductMapper;
import com.example.product_service.model.Product;
import com.example.product_service.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public Product getProduct(long productId) {
        return repository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    public ProductDetailsDTO createProduct(ProductRequest request) {
        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory().toLowerCase());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        Product saved = repository.save(product);

        return ProductMapper.toDetailsDTO(saved);
    }

    public List<ProductSummaryDTO> getProducts(String category, BigDecimal maxPrice) {
        List<Product> products = repository.findProducts(category, maxPrice);

        return products.stream()
                .map(ProductMapper::toSummaryDTO)
                .toList();
    }

    public ProductDetailsDTO getProductById(long productId) {
        Product product = getProduct(productId);
        return ProductMapper.toDetailsDTO(product);
    }

    public ProductDetailsDTO updateProduct(long productId, ProductRequest request) {
        Product product = getProduct(productId);

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory().toLowerCase());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        Product updated = repository.save(product);
        return ProductMapper.toDetailsDTO(updated);
    }

    public void deleteProduct(long productId) {
        Product product = getProduct(productId);
        repository.delete(product);
    }

    // Inventory Management

    @Transactional
    public Product reduceStock(Long productId, Integer quantity) {
        Product product = repository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (product.getStock() < quantity) {
            throw new ConflictException("Insufficient stock for product ID " + productId);
        }

        product.setStock(product.getStock() - quantity);
        return repository.save(product);
    }

    @Transactional
    public Product increaseStock(Long productId, Integer quantity) {
        Product product = repository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setStock(product.getStock() + quantity);
        return repository.save(product);
    }
}
