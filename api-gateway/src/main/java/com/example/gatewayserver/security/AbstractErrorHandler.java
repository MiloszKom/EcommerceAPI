package com.example.gatewayserver.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public abstract class AbstractErrorHandler {
    protected final ObjectMapper objectMapper;

    protected AbstractErrorHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    protected Mono<Void> writeErrorResponse(ServerWebExchange webExchange, HttpStatus status, String message) {
        ServerHttpResponse response = webExchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                status.value(),
                message,
                LocalDateTime.now(),
                webExchange.getRequest().getPath().toString()
        );

        try {
            byte[] errorBytes = objectMapper.writeValueAsBytes(errorResponse);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(errorBytes)))
                    .doOnError(error -> response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR))
                    .then();
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}
