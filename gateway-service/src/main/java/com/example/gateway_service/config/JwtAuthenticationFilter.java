package com.example.gateway_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtTokenHelper jwtTokenHelper;

    public JwtAuthenticationFilter(JwtTokenHelper jwtTokenHelper) {
        this.jwtTokenHelper = jwtTokenHelper;
    }

    private static final String AUTHORIZATION = "Authorization";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestToken = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION);

        if (requestToken != null && requestToken.startsWith("Bearer ")) {
            String token = requestToken.substring(7);

            if (jwtTokenHelper.validateToken(token)) {
                UserResponse user = jwtTokenHelper.extractPayloadFromToken(token);
                List<SimpleGrantedAuthority> authorities =
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
                SecurityContext context = new SecurityContextImpl(
                        new UsernamePasswordAuthenticationToken(user, null, authorities));

                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("userId", user.getUserId())
                        .header("email", user.getEmail())
                        .header("role", user.getRole())
                        .build();

                exchange = exchange.mutate().request(modifiedRequest).build();

                return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
            } else {
                LOGGER.error("JWT token is malformed or expired: {}", token);
            }
        } else {
            LOGGER.debug("No JWT token found for request: {}", exchange.getRequest().getPath());
        }

        return chain.filter(exchange);
    }
}