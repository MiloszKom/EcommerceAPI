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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements IProductService {
    private final ProductRepository repository;

    public ProductServiceImpl(ProductRepository repository) {
        this.repository = repository;
    }

    private Product getProduct(long productId) {
        return repository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "Id", productId));
    }

    @Override
    public List<ProductSummaryDto> getProducts() {
        List<Product> products = repository.findAll();

        return products.stream()
                .map(ProductMapper::toSummaryDto)
                .toList();
    }

    @Override
    public ProductDetailsDto getProductById(long productId) {
        Product product = getProduct(productId);
        return ProductMapper.toDetailsDto(product);
    }

    @Override
    public ProductDetailsDto createProduct(ProductRequestDto request) {
        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        Product saved = repository.save(product);

        return ProductMapper.toDetailsDto(saved);
    }

    @Override
    public ProductDetailsDto updateProduct(long productId, ProductRequestDto request) {
        Product product = getProduct(productId);

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        Product updated = repository.save(product);
        return ProductMapper.toDetailsDto(updated);
    }

    @Override
    public void deleteProduct(long productId) {
        Product product = getProduct(productId);
        repository.delete(product);
    }

    // Inventory Management

    @Transactional
    public void reduceStock(long productId, Integer quantity) {
        Product product = getProduct(productId);

        if (product.getStock() < quantity) {
            throw new ConflictException("Insufficient stock for product ID " + productId);
        }

        product.setStock(product.getStock() - quantity);
        repository.save(product);
    }

    @Transactional
    public void increaseStock(long productId, Integer quantity) {
        Product product = getProduct(productId);

        product.setStock(product.getStock() + quantity);
        repository.save(product);
    }
}
