package com.example.gateway_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    private static final String[] PUBLIC_ENDPOINTS = {"/api/auth/login", "/api/auth/register"};
    private static final String[] ADMIN_POST_ENDPOINTS = {"/api/products", "/api/orders/*/complete"};
    private static final String[] ADMIN_PUT_ENDPOINTS = {"/api/products/**"};
    private static final String[] ADMIN_DELETE_ENDPOINTS = {"/api/products/**"};
    private static final String[] ADMIN_GET_ENDPOINTS = {"/api/users", "/api/orders"};

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        // Public endpoints (no authentication)
                        .pathMatchers(PUBLIC_ENDPOINTS).permitAll()
                        // Admin-only endpoints
                        .pathMatchers(HttpMethod.POST, ADMIN_POST_ENDPOINTS).hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, ADMIN_PUT_ENDPOINTS).hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, ADMIN_DELETE_ENDPOINTS).hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, ADMIN_GET_ENDPOINTS).hasRole("ADMIN")
                        .anyExchange().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .build();
    }
}