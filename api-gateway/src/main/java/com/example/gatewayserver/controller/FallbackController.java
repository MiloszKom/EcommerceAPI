package com.example.gatewayserver.controller;

import com.example.gatewayserver.security.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/order-service")
    public Mono<ResponseEntity<ErrorResponseDto>> orderServiceFallback(ServerWebExchange exchange) {
        return buildFallbackResponse("Order service", exchange);
    }

    @RequestMapping("/product-service")
    public Mono<ResponseEntity<ErrorResponseDto>> productServiceFallback(ServerWebExchange exchange) {
        return buildFallbackResponse("Product service", exchange);
    }

    @RequestMapping("/cart-service")
    public Mono<ResponseEntity<ErrorResponseDto>> cartServiceFallback(ServerWebExchange exchange) {
        return buildFallbackResponse("Cart service", exchange);
    }

    @RequestMapping("/user-service")
    public Mono<ResponseEntity<ErrorResponseDto>> userServiceFallback(ServerWebExchange exchange) {
        return buildFallbackResponse("User service", exchange);
    }

    private Mono<ResponseEntity<ErrorResponseDto>> buildFallbackResponse(String serviceName, ServerWebExchange exchange) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                String.format("%s is currently unavailable. Please try again later.", serviceName),
                LocalDateTime.now(),
                exchange.getRequest().getPath().toString()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse));
    }
}
