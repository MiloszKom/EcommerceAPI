package com.example.order_service.client;

import com.example.order_service.config.FeignConfig;
import com.example.order_service.dto.CartDetailsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "cart-service", url = "${cart.service.url}", configuration = FeignConfig.class)
public interface CartClient {

    @GetMapping("/api/cart")
    CartDetailsDTO getUserCart();

    @DeleteMapping("/api/cart/clear")
    void clearCart();
}
