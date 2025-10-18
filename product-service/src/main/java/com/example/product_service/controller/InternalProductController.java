package com.example.product_service.controller;

import com.example.product_service.dto.StockUpdateRequest;
import com.example.product_service.service.IProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/products")
@Validated
public class InternalProductController {

    private final IProductService productService;

    public InternalProductController(IProductService productService) {
        this.productService = productService;
    }

    @PutMapping("/{productId}/reduce-stock")
    public ResponseEntity<Void> reduceStock(
            @Valid @PathVariable Long productId,
            @Valid @RequestBody StockUpdateRequest request
    ) {
        productService.reduceStock(productId, request.quantity());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{productId}/increase-stock")
    public ResponseEntity<Void> increaseStock(
            @Valid @PathVariable Long productId,
            @Valid @RequestBody StockUpdateRequest request
    ) {
        productService.increaseStock(productId, request.quantity());
        return ResponseEntity.ok().build();
    }
}
