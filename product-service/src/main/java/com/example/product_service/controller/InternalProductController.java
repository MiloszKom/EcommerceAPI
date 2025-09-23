package com.example.product_service.controller;

import com.example.product_service.dto.StockUpdateRequest;
import com.example.product_service.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/products")
public class InternalProductController {

    @Autowired
    private ProductService service;

    @PutMapping("/{productId}/reduce-stock")
    public ResponseEntity<Void> reduceStock(
            @PathVariable Long productId,
            @RequestBody StockUpdateRequest request
    ) {
        service.reduceStock(productId, request.quantity());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{productId}/increase-stock")
    public ResponseEntity<Void> increaseStock(
            @PathVariable Long productId,
            @RequestBody StockUpdateRequest request
    ) {
        service.increaseStock(productId, request.quantity());
        return ResponseEntity.ok().build();
    }
}
