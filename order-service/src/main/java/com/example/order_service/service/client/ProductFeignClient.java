package com.example.order_service.service.client;

import com.example.order_service.dto.client.StockUpdateRequest;
import com.example.order_service.dto.client.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="product-service",  fallbackFactory = ProductFallbackFactory.class)
public interface ProductFeignClient {

    @GetMapping(value = "/api/products/{id}", consumes = "application/json")
    ProductDto getProductById(@PathVariable("id") Long productId);

    @PutMapping(value = "/internal/products/{id}/reduce-stock", consumes = "application/json")
    void reduceStock(
            @PathVariable("id") Long productId,
            @RequestBody StockUpdateRequest request
    );

    @PutMapping(value = "/internal/products/{id}/increase-stock", consumes = "application/json")
    void increaseStock(
            @PathVariable("id") Long productId,
            @RequestBody StockUpdateRequest request
    );
}
