package com.example.gatewayserver.config;

import com.example.gatewayserver.exception.ErrorResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

@Component
public class CustomWebFluxAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomWebFluxAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String message;
        if (ex.getCause() instanceof org.springframework.security.oauth2.jwt.JwtException) {
            message = "The access token provided is expired, revoked, malformed, or invalid.";
        } else {
            message = "Authentication is required to access this resource.";
        }

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                401,
                message,
                LocalDateTime.now(),
                exchange.getRequest().getPath().value()
        );

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorResponse);
        } catch (Exception e) {
            bytes = ("{\"status\":401,\"message\":\"" + message + "\"}").getBytes();
        }

        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }
}