package com.example.product_service.controller;

import com.example.product_service.dto.ProductDetailsDto;
import com.example.product_service.dto.ProductRequestDto;
import com.example.product_service.dto.ProductSummaryDto;
import com.example.product_service.service.IProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final IProductService productService;

    public ProductController(IProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductSummaryDto>> getProducts() {
        log.info("GET /api/products - Fetching all products");
        List<ProductSummaryDto> products = productService.getProducts();
        log.debug("Fetched {} products", products.size());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailsDto> getProductById(@PathVariable Long id) {
        log.info("GET /api/products/{} - Fetching product details", id);
        ProductDetailsDto product = productService.getProductById(id);
        log.debug("Fetched product details: {}", product);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<ProductDetailsDto> createProduct(@Valid @RequestBody ProductRequestDto product) {
        log.info("POST /api/products - Creating product with name: {}", product.getName());
        ProductDetailsDto created = productService.createProduct(product);
        log.info("Created product with ID: {}", created.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDetailsDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDto product
    ) {
        log.info("PUT /api/products/{} - Updating product", id);
        ProductDetailsDto updated = productService.updateProduct(id, product);
        log.info("Updated product ID: {}", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("DELETE /api/products/{} - Deleting product", id);
        productService.deleteProduct(id);
        log.info("Deleted product ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
