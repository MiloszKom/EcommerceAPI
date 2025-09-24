package com.example.gateway_service.controller;

import com.example.gateway_service.config.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class FallbackController {

    private Mono<ResponseEntity<ErrorResponse>> buildFallbackResponse(String message) {
        ErrorResponse response = new ErrorResponse(message, HttpStatus.SERVICE_UNAVAILABLE.value());
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @RequestMapping("/fallback/users")
    public Mono<ResponseEntity<ErrorResponse>> userFallback() {
        return buildFallbackResponse("User service is currently unavailable. Please try again later.");
    }

    @RequestMapping("/fallback/products")
    public Mono<ResponseEntity<ErrorResponse>> productFallback() {
        return buildFallbackResponse("Product service is currently unavailable. Please try again later.");
    }

//    @RequestMapping("/fallback/cart")
//    public Mono<ResponseEntity<ErrorResponse>> cartFallback() {
//        return buildFallbackResponse("Cart service is currently unavailable. Please try again later.");
//    }

//    @RequestMapping("/fallback/orders")
//    public Mono<ResponseEntity<ErrorResponse>> orderFallback() {
//        return buildFallbackResponse("Order service is currently unavailable. Please try again later.");
//    }
}