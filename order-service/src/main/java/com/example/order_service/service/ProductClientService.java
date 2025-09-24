package com.example.order_service.service;

import com.example.order_service.client.ProductClient;
import com.example.order_service.dto.ProductDTO;
import com.example.order_service.dto.StockUpdateRequest;
import com.example.order_service.exception.types.RemoteServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ProductClientService {

    private final ProductClient productClient;

    public ProductClientService (ProductClient productClient) {
        this.productClient = productClient;
    }

    @CircuitBreaker(name = "orderServiceCircuitBreaker", fallbackMethod = "fallbackGetProductById")
    @Retry(name = "orderServiceRetry")
    public ProductDTO getProductById(long productId) {
        return productClient.getProductById(productId);
    }

    private ProductDTO fallbackGetProductById(long productId, Throwable throwable) {
        if (throwable instanceof RemoteServiceException) {
            throw (RemoteServiceException) throwable;
        }

        throw new RemoteServiceException(
                "product-service",
                HttpStatus.SERVICE_UNAVAILABLE,
                "Product service is unavailable."
        );
    }

    @CircuitBreaker(name = "productServiceCircuitBreaker", fallbackMethod = "fallbackReduceStock")
    @Retry(name = "productServiceRetry")
    public void reduceProductStock(Long productId, StockUpdateRequest request) {
        productClient.reduceProductStock(productId, request);
    }

    private void fallbackReduceStock(Long productId, StockUpdateRequest request, Throwable throwable) {
        if (throwable instanceof RemoteServiceException) {
            throw (RemoteServiceException) throwable;
        }

        throw new RemoteServiceException(
                "product-service",
                HttpStatus.SERVICE_UNAVAILABLE,
                "Product service is unavailable."
        );
    }

    @CircuitBreaker(name = "productServiceCircuitBreaker", fallbackMethod = "fallbackIncreaseStock")
    @Retry(name = "productServiceRetry")
    public void increaseProductStock(Long productId, StockUpdateRequest request) {
        productClient.increaseProductStock(productId, request);
    }

    private void fallbackIncreaseStock(Long productId, StockUpdateRequest request, Throwable throwable) {
        if (throwable instanceof RemoteServiceException) {
            throw (RemoteServiceException) throwable;
        }

        throw new RemoteServiceException(
                "product-service",
                HttpStatus.SERVICE_UNAVAILABLE,
                "Product service is unavailable."
        );
    }
}
