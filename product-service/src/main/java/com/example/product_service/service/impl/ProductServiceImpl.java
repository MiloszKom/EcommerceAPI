package com.example.product_service.service.impl;

import com.example.product_service.dto.ProductDetailsDto;
import com.example.product_service.dto.ProductRequestDto;
import com.example.product_service.dto.ProductSummaryDto;
import com.example.product_service.entity.Product;
import com.example.product_service.exception.ConflictException;
import com.example.product_service.exception.ResourceNotFoundException;
import com.example.product_service.mapper.ProductMapper;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.service.IProductService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements IProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository repository;

    public ProductServiceImpl(ProductRepository repository) {
        this.repository = repository;
    }

    private Product getProduct(long productId) {
        log.debug("Fetching product with ID: {}", productId);
        return repository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Product not found with ID: {}", productId);
                    return new ResourceNotFoundException("Product", "productId", productId);
                });
    }

    @Override
    public List<ProductSummaryDto> getProducts() {
        log.debug("Retrieving all products from repository");
        List<Product> products = repository.findAll();
        log.info("Retrieved {} products", products.size());
        return products.stream()
                .map(ProductMapper::toSummaryDto)
                .toList();
    }

    @Override
    public ProductDetailsDto getProductById(long productId) {
        log.info("Fetching product details for ID: {}", productId);
        Product product = getProduct(productId);
        log.debug("Converting product to DTO for ID: {}", productId);
        return ProductMapper.toDetailsDto(product);
    }

    @Override
    public ProductDetailsDto createProduct(ProductRequestDto request) {
        log.info("Creating new product: {}", request.name());
        Product product = new Product();

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());

        Product saved = repository.save(product);
        log.debug("Product saved with ID: {}", saved.getId());;
        return ProductMapper.toDetailsDto(saved);
    }

    @Override
    public ProductDetailsDto updateProduct(long productId, ProductRequestDto request) {
        log.info("Updating product: {}", request.name());
        Product product = getProduct(productId);

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());

        Product updated = repository.save(product);
        log.info("Updated product with ID: {}", productId);
        return ProductMapper.toDetailsDto(updated);
    }

    @Override
    public void deleteProduct(long productId) {
        log.info("Deleting product with ID: {}", productId);
        Product product = getProduct(productId);
        repository.delete(product);
        log.info("Deleted product with ID: {}", productId);
    }

    // Inventory Management

    @Transactional
    public void reduceStock(long productId, Integer quantity) {
        log.info("Reducing stock for product {} by {}", productId, quantity);
        Product product = getProduct(productId);

        if (product.getStock() < quantity) {
            log.error("Insufficient stock for product ID: {}. Current stock: {}, Requested: {}",
                    productId, product.getStock(), quantity);
            throw new ConflictException("Insufficient stock for product ID " + productId);
        }

        product.setStock(product.getStock() - quantity);
        repository.save(product);
        log.debug("Stock reduced successfully for product ID: {}", productId);
    }

    @Transactional
    public void increaseStock(long productId, Integer quantity) {
        log.info("Increasing stock for product {} by {}", productId, quantity);
        Product product = getProduct(productId);

        product.setStock(product.getStock() + quantity);
        repository.save(product);
        log.debug("Stock increased successfully for product ID: {}", productId);
    }
}
