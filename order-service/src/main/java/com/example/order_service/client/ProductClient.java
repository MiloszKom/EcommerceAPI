package com.example.order_service.client;

import com.example.order_service.config.FeignConfig;
import com.example.order_service.dto.ProductDTO;
import com.example.order_service.dto.StockUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product-service", url = "${product.service.url}", configuration = FeignConfig.class)
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductDTO getProductById(@PathVariable("id") long id);

    @PutMapping("/internal/products/{productId}/reduce-stock")
    void reduceProductStock(@PathVariable("productId") Long productId,
                            @RequestBody StockUpdateRequest request);

    @PutMapping("/internal/products/{productId}/increase-stock")
    void increaseProductStock(@PathVariable("productId") Long productId,
                            @RequestBody StockUpdateRequest request);
}
