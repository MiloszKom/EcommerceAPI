package com.example.cart_service.service;

import com.example.cart_service.client.ProductClient;
import com.example.cart_service.dto.ProductDTO;
import com.example.cart_service.exception.types.RemoteServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ProductClientService {

    private final ProductClient productClient;

    public ProductClientService(ProductClient productClient) {
        this.productClient = productClient;
    }

    @CircuitBreaker(name = "productServiceCircuitBreaker", fallbackMethod = "fallbackProduct")
    @Retry(name = "productServiceRetry")
    public ProductDTO getProductById(Long productId) {
        return productClient.getProductById(productId);
    }

    private ProductDTO fallbackProduct(Long productId, Throwable throwable) {
        if (throwable instanceof RemoteServiceException) {
            throw (RemoteServiceException) throwable;
        }

        throw new RemoteServiceException(
                "product-service",
                HttpStatus.SERVICE_UNAVAILABLE,
                "Product service is unavailable, failed to fetch product " + productId
        );
    }
}