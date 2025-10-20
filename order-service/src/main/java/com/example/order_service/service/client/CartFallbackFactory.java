package com.example.order_service.service.client;

import com.example.order_service.dto.client.CartDto;
import com.example.order_service.exception.ExternalServiceException;
import com.example.order_service.service.impl.OrderServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

@Component
public class CartFallbackFactory implements FallbackFactory<CartFeignClient> {

    private static final Logger log = LoggerFactory.getLogger(CartFallbackFactory.class);
    String DEFAULT_MESSAGE = "Cannot retrieve cart details: Cart service is temporarily unavailable";

    @Override
    public CartFeignClient create(Throwable cause) {
        log.error("Fallback triggered for CartFeignClient due to: {}", cause.getMessage(), cause);

        Throwable rootCause = cause;
        if (cause instanceof ExecutionException || cause instanceof CompletionException) {
            if (cause.getCause() != null) {
                rootCause = cause.getCause();
            }
        }

        Throwable finalCause = rootCause;


        return new CartFeignClient() {
            private <T> T executeFallback() {
                if (finalCause instanceof FeignException fe) {
                    HttpStatus status = HttpStatus.valueOf(fe.status());
                    String message = extractErrorMessage(fe);

                    log.warn("FeignException occurred: HTTP {}, Message: {}", status, message);
                    throw new ExternalServiceException(message, status);
                } else {
                    log.error("Unexpected error in fallback: {}", finalCause.getMessage());
                    throw new ExternalServiceException(
                            DEFAULT_MESSAGE,
                            HttpStatus.SERVICE_UNAVAILABLE
                    );
                }
            }

            @Override
            public CartDto getUserCart(@RequestHeader("X-User-Id") String userId) {
                return executeFallback();
            }

            @Override
            public void clearCart(@RequestHeader("X-User-Id") String userId) {
                executeFallback();
            }
        };
    }

    private String extractErrorMessage(FeignException e) {
        try {
            String json = e.contentUTF8();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(json);
            String message = jsonNode.get("message").asText();
            log.debug("Extracted error message: {}", message);

            return jsonNode.get("message").asText();
        } catch (Exception ex) {
            log.error("Failed to extract error message from FeignException: {}", ex.getMessage(), ex);
            return DEFAULT_MESSAGE;
        }
    }
}