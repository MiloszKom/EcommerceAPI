package com.example.gatewayserver.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class JwtForwardingGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtForwardingGatewayFilterFactory.Config> {
    public JwtForwardingGatewayFilterFactory() {
        super(Config.class);
    }

    private static final Logger log = LoggerFactory.getLogger(JwtForwardingGatewayFilterFactory.class);

    public static class Config {}

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader != null) {
                log.info("Forwarding request with Authorization header: {}", authHeader);
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .build();
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            }
            return chain.filter(exchange);
        };
    }
}