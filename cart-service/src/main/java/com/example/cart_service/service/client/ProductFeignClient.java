package com.example.cart_service.service.client;

import com.example.cart_service.dto.client.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="product-service", fallbackFactory = ProductFallbackFactory.class)
public interface ProductFeignClient {

    @GetMapping(value = "/api/products/{id}", consumes = "application/json")
    ProductDto getProductById(@PathVariable("id") Long productId);
}
