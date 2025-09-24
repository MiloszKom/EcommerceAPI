package com.example.order_service.service;

import com.example.order_service.client.CartClient;
import com.example.order_service.dto.CartDetailsDTO;
import com.example.order_service.exception.types.RemoteServiceException;
import com.example.order_service.exception.types.ServiceCommunicationException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CartClientService {

    private final CartClient cartClient;

    public CartClientService (CartClient cartClient) {
        this.cartClient = cartClient;
    }

    @CircuitBreaker(name = "orderServiceCircuitBreaker", fallbackMethod = "fallbackGetUserCart")
    @Retry(name = "orderServiceRetry")
    public CartDetailsDTO getUserCart() {
        return cartClient.getUserCart();
    }

    private CartDetailsDTO fallbackGetUserCart(Throwable throwable) {
        if (throwable instanceof RemoteServiceException) {
            throw (RemoteServiceException) throwable;
        }

        throw new RemoteServiceException(
                "cart-service",
                HttpStatus.SERVICE_UNAVAILABLE,
                "Cart service is unavailable."
        );
    }

    @CircuitBreaker(name = "orderServiceCircuitBreaker", fallbackMethod = "fallbackClearCart")
    @Retry(name = "orderServiceRetry")
    public void clearCart() {
        cartClient.clearCart();
    }

    private void fallbackClearCart(Throwable throwable) {
        if (throwable instanceof RemoteServiceException) {
            throw (RemoteServiceException) throwable;
        }

        throw new RemoteServiceException(
                "cart-service",
                HttpStatus.SERVICE_UNAVAILABLE,
                "Cart service is unavailable."
        );
    }
}
