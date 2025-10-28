package com.example.product_service.controller;

import com.example.product_service.dto.StockUpdateRequest;
import com.example.product_service.service.IProductService;
import com.example.product_service.service.impl.ProductServiceImpl;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/products")
@Validated
@Hidden
public class InternalProductController {
    private static final Logger log = LoggerFactory.getLogger(InternalProductController.class);
    private final IProductService productService;

    public InternalProductController(IProductService productService) {
        this.productService = productService;
    }

    @PutMapping("/{productId}/reduce-stock")
    public ResponseEntity<Void> reduceStock(
            @Valid @PathVariable Long productId,
            @Valid @RequestBody StockUpdateRequest request
    ) {
        log.info("Received internal request to reduce stock for product ID {} by {}", productId, request.quantity());
        productService.reduceStock(productId, request.quantity());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{productId}/increase-stock")
    public ResponseEntity<Void> increaseStock(
            @Valid @PathVariable Long productId,
            @Valid @RequestBody StockUpdateRequest request
    ) {
        log.info("Received internal request to increase stock for product ID {} by {}", productId, request.quantity());
        productService.increaseStock(productId, request.quantity());
        return ResponseEntity.ok().build();
    }
}
