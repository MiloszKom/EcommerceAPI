package com.example.product_service.controller;

import com.example.product_service.dto.ProductDetailsDTO;
import com.example.product_service.dto.ProductRequest;
import com.example.product_service.dto.ProductSummaryDTO;
import com.example.product_service.dto.StockUpdateRequest;
import com.example.product_service.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService service;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProductDetailsDTO> createProduct(@Valid @RequestBody ProductRequest product) {
        ProductDetailsDTO created = service.createProduct(product);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<ProductSummaryDTO>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        List<ProductSummaryDTO> products = service.getProducts(category, maxPrice);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailsDTO> getProductById(@PathVariable Long id) {
        ProductDetailsDTO product = service.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductDetailsDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequest product
    ) {
        ProductDetailsDTO updated = service.updateProduct(id, product);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        service.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{productId}/reduce-stock")
    public ResponseEntity<Void> reduceStock(@PathVariable Long productId, @RequestBody StockUpdateRequest request) {
        service.reduceStock(productId, request.quantity());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{productId}/increase-stock")
    public ResponseEntity<Void> increaseStock(@PathVariable Long productId, @RequestBody StockUpdateRequest request) {
        service.increaseStock(productId, request.quantity());
        return ResponseEntity.ok().build();
    }
}
