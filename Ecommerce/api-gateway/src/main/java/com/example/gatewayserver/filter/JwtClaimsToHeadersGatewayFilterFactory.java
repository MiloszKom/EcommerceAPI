package com.example.gatewayserver.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Map;

@Component
public class JwtClaimsToHeadersGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtClaimsToHeadersGatewayFilterFactory.Config> {

    public JwtClaimsToHeadersGatewayFilterFactory() {
        super(Config.class);
    }

    public static class Config {}

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) ->
                ReactiveSecurityContextHolder.getContext()
                        .flatMap(securityContext -> {
                            if (securityContext.getAuthentication() instanceof JwtAuthenticationToken jwtAuth) {
                                Jwt jwt = jwtAuth.getToken();

                                String email = jwt.getClaimAsString("email");
                                String preferredUsername = jwt.getClaimAsString("preferred_username");
                                String sub = jwt.getClaimAsString("sub");

                                Map<String, Object> realmAccess = jwt.getClaim("realm_access");
                                boolean isAdmin = false;

                                if (realmAccess != null && realmAccess.get("roles") instanceof List<?> rolesList) {
                                    isAdmin = rolesList.contains("admin");
                                }

                                ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();

                                if (email != null)
                                    requestBuilder.header("X-User-Email", email);
                                if (preferredUsername != null)
                                    requestBuilder.header("X-User-Username", preferredUsername);
                                if (sub != null)
                                    requestBuilder.header("X-User-Id", sub);

                                requestBuilder.header("X-User-IsAdmin", String.valueOf(isAdmin));

                                ServerWebExchange modifiedExchange = exchange.mutate()
                                        .request(requestBuilder.build())
                                        .build();

                                return chain.filter(modifiedExchange);
                            }
                            return chain.filter(exchange);
                        })
                        .switchIfEmpty(chain.filter(exchange));
    }
}