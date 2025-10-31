package com.example.gatewayserver;

import com.example.gatewayserver.filter.JwtForwardingGatewayFilterFactory;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "API Gateway", version = "1.0", description = "Documentation API Gateway v1.0"))
public class ApiGatewayApplication {

	@Autowired
	private JwtForwardingGatewayFilterFactory jwtForwardingFilter;

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

    @Value("${EXTERNAL_KEYCLOAK_URL}")
    private String keycloakUrl;

	@Bean
	public RouteLocator routeConfig(RouteLocatorBuilder routeLocatorBuilder) {
		return routeLocatorBuilder.routes()
                .route(r -> r.path("/user-service/v3/api-docs")
                        .and()
                        .method(HttpMethod.GET)
                        .filters(f -> f.rewritePath("/user-service/(?<remaining>.*)", "/${remaining}"))
                        .uri("lb://USER-SERVICE"))

                .route("user-service", r -> r
						.path("/api/users/**")
						.filters(f -> f
								.rewritePath("/api/users/(?<segment>.*)", "/api/users/${segment}")
								.filter(jwtForwardingFilter.apply(new JwtForwardingGatewayFilterFactory.Config()))
								.circuitBreaker(c -> c
										.setName("defaultServiceCircuitBreaker")
										.setFallbackUri("forward:/fallback/user-service"))
						)
						.uri("lb://USER-SERVICE"))

                .route(r -> r.path("/product-service/v3/api-docs")
                        .and()
                        .method(HttpMethod.GET)
                        .filters(f -> f.rewritePath("/product-service/(?<remaining>.*)", "/${remaining}"))
                        .uri("lb://PRODUCT-SERVICE"))

                .route("product-service", r -> r
						.path("/api/products/**")
						.filters(f -> f
								.rewritePath("/api/products/(?<segment>.*)", "/api/products/${segment}")
								.filter(jwtForwardingFilter.apply(new JwtForwardingGatewayFilterFactory.Config()))
								.circuitBreaker(c -> c
										.setName("defaultServiceCircuitBreaker")
										.setFallbackUri("forward:/fallback/product-service"))
						)
						.uri("lb://PRODUCT-SERVICE"))

                .route(r -> r.path("/cart-service/v3/api-docs")
                        .and()
                        .method(HttpMethod.GET)
                        .filters(f -> f.rewritePath("/cart-service/(?<remaining>.*)", "/${remaining}"))
                        .uri("lb://CART-SERVICE"))

				.route("cart-service", r -> r
						.path("/api/cart/**")
						.filters(f -> f
								.rewritePath("/api/cart/(?<segment>.*)", "/api/cart/${segment}")
								.filter(jwtForwardingFilter.apply(new JwtForwardingGatewayFilterFactory.Config()))
								.circuitBreaker(c -> c
										.setName("defaultServiceCircuitBreaker")
										.setFallbackUri("forward:/fallback/cart-service"))
						)
						.uri("lb://CART-SERVICE"))

                .route(r -> r.path("/order-service/v3/api-docs")
                        .and()
                        .method(HttpMethod.GET)
                        .filters(f -> f.rewritePath("/order-service/(?<remaining>.*)", "/${remaining}"))
                        .uri("lb://ORDER-SERVICE"))

				.route("order-service", r -> r
						.path("/api/orders/**")
						.filters(f -> f
								.rewritePath("/api/orders/(?<segment>.*)", "/api/orders/${segment}")
								.filter(jwtForwardingFilter.apply(new JwtForwardingGatewayFilterFactory.Config()))
								.circuitBreaker(c -> c
										.setName("defaultServiceCircuitBreaker")
										.setFallbackUri("forward:/fallback/order-service"))
						)
						.uri("lb://ORDER-SERVICE"))

                .route("keycloak-auth", r -> r
                        .path("/keycloak/auth")
                        .filters(f -> f
                                .rewritePath("/keycloak/auth", "/realms/ecommerce-realm/protocol/openid-connect/auth")
                                .setResponseHeader("Access-Control-Allow-Origin", "*") // For CORS, adjust as needed
                        )
                        .uri(keycloakUrl))
                .route("keycloak-token", r -> r
                        .path("/keycloak/token")
                        .filters(f -> f
                                .rewritePath("/keycloak/token", "/realms/ecommerce-realm/protocol/openid-connect/token")
                                .setResponseHeader("Access-Control-Allow-Origin", "*")
                        )
                        .uri(keycloakUrl))
                .route("keycloak-userinfo", r -> r
                        .path("/keycloak/userinfo")
                        .filters(f -> f
                                .rewritePath("/keycloak/userinfo", "/realms/ecommerce-realm/protocol/openid-connect/userinfo")
                                .setResponseHeader("Access-Control-Allow-Origin", "*")
                        )
                        .uri(keycloakUrl))
                .route("keycloak-logout", r -> r
                        .path("/keycloak/logout")
                        .filters(f -> f
                                .rewritePath("/keycloak/logout", "/realms/ecommerce-realm/protocol/openid-connect/logout")
                                .setResponseHeader("Access-Control-Allow-Origin", "*")
                        )
                        .uri(keycloakUrl))
                .route("keycloak-certs", r -> r
                        .path("/keycloak/certs")
                        .filters(f -> f
                                .rewritePath("/keycloak/certs", "/realms/ecommerce-realm/protocol/openid-connect/certs")
                                .setResponseHeader("Access-Control-Allow-Origin", "*")
                        )
                        .uri(keycloakUrl))
				.build();
	}
}