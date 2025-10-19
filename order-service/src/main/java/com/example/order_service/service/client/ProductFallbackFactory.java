package com.example.order_service.service.client;

import com.example.order_service.dto.client.ProductDto;
import com.example.order_service.exception.ExternalServiceException;
import com.example.order_service.dto.client.StockUpdateRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

@Component
public class ProductFallbackFactory implements FallbackFactory<ProductFeignClient> {

    String defaultMessage = "Cannot retrieve product details: Product service is temporarily unavailable";

    @Override
    public ProductFeignClient create(Throwable cause) {
        Throwable rootCause = cause;
        if (cause instanceof ExecutionException || cause instanceof CompletionException) {
            if (cause.getCause() != null) {
                rootCause = cause.getCause();
            }
        }

        Throwable finalCause = rootCause;

        return new ProductFeignClient() {
            private <T> T executeFallback() {
                if (finalCause instanceof FeignException fe) {
                    HttpStatus status = HttpStatus.valueOf(fe.status());
                    String message = extractErrorMessage(fe);
                    throw new ExternalServiceException(message, status);
                } else {
                    throw new ExternalServiceException(
                            defaultMessage,
                            HttpStatus.SERVICE_UNAVAILABLE
                    );
                }
            }

            @Override
            public ProductDto getProductById(Long productId) {
                return executeFallback();
            }

            @Override
            public void reduceStock(Long productId, StockUpdateRequest request) {
                executeFallback();
            }

            @Override
            public void increaseStock(Long productId, StockUpdateRequest request) {
                executeFallback();
            }
        };
    }

    private String extractErrorMessage(FeignException e) {
        try {
            String json = e.contentUTF8();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(json);
            return jsonNode.get("message").asText();
        } catch (Exception ex) {
            return defaultMessage;
        }
    }
}
