package com.example.gatewayserver.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class CustomAccessDeniedHandler extends AbstractErrorHandler implements ServerAccessDeniedHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange webExchange, AccessDeniedException deniedException) {
        return writeErrorResponse(webExchange, HttpStatus.FORBIDDEN, "Access denied: You do not have the required privileges to access this resource.");
    }
}