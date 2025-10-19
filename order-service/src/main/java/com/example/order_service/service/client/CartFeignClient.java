package com.example.order_service.service.client;

import com.example.order_service.dto.client.CartDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name="cart-service", fallbackFactory = CartFallbackFactory.class)
public interface CartFeignClient {

    @GetMapping(value = "/api/cart", consumes = "application/json")
    CartDto getUserCart(@RequestHeader("X-User-Id") String userId);

    @DeleteMapping(value = "/api/cart/clear", consumes = "application/json")
    void clearCart(@RequestHeader("X-User-Id") String userId);
}
