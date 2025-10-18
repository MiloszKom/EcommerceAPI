package com.example.gatewayserver.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class CustomAuthenticationEntryPoint extends AbstractErrorHandler implements ServerAuthenticationEntryPoint {
    @Override
    public Mono<Void> commence(ServerWebExchange webExchange, AuthenticationException authException) {
        return writeErrorResponse(webExchange, HttpStatus.UNAUTHORIZED, "Authentication is required to access this resource.");
    }
}
