package com.example.gatewayserver.security;

import com.example.gatewayserver.config.SecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class CustomAuthenticationEntryPoint extends AbstractErrorHandler implements ServerAuthenticationEntryPoint {
    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);

    @Override
    public Mono<Void> commence(ServerWebExchange webExchange, AuthenticationException authException) {
        log.error("🚫 Authentication failed: {}", authException.getMessage(), authException);
        return writeErrorResponse(webExchange, HttpStatus.UNAUTHORIZED, "Authentication is required to access this resource.");
    }
}
