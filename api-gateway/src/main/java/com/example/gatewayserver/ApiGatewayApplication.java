package com.example.gatewayserver;

import com.example.gatewayserver.filter.JwtClaimsToHeadersGatewayFilterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApiGatewayApplication {

	@Autowired
	private JwtClaimsToHeadersGatewayFilterFactory jwtClaimsFilter;

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@Bean
	public RouteLocator routeConfig(RouteLocatorBuilder routeLocatorBuilder) {
		return routeLocatorBuilder.routes()
				.route("user-service", r -> r
						.path("/api/users/**")
						.filters(f -> f
								.rewritePath("/api/users/(?<segment>.*)", "/api/users/${segment}")
								.filter(jwtClaimsFilter.apply(new JwtClaimsToHeadersGatewayFilterFactory.Config()))
								.circuitBreaker(c -> c
										.setName("defaultServiceCircuitBreaker")
										.setFallbackUri("forward:/fallback/user-service"))
						)
						.uri("lb://USER-SERVICE"))

				.route("product-service", r -> r
						.path("/api/products/**")
						.filters(f -> f
								.rewritePath("/api/products/(?<segment>.*)", "/api/products/${segment}")
								.filter(jwtClaimsFilter.apply(new JwtClaimsToHeadersGatewayFilterFactory.Config()))
								.circuitBreaker(c -> c
										.setName("defaultServiceCircuitBreaker")
										.setFallbackUri("forward:/fallback/product-service"))
						)
						.uri("lb://PRODUCT-SERVICE"))

				.route("cart-service", r -> r
						.path("/api/cart/**")
						.filters(f -> f
								.rewritePath("/api/cart/(?<segment>.*)", "/api/cart/${segment}")
								.filter(jwtClaimsFilter.apply(new JwtClaimsToHeadersGatewayFilterFactory.Config()))
								.circuitBreaker(c -> c
										.setName("defaultServiceCircuitBreaker")
										.setFallbackUri("forward:/fallback/cart-service"))
						)
						.uri("lb://CART-SERVICE"))

				.route("order-service", r -> r
						.path("/api/orders/**")
						.filters(f -> f
								.rewritePath("/api/orders/(?<segment>.*)", "/api/orders/${segment}")
								.filter(jwtClaimsFilter.apply(new JwtClaimsToHeadersGatewayFilterFactory.Config()))
								.circuitBreaker(c -> c
										.setName("defaultServiceCircuitBreaker")
										.setFallbackUri("forward:/fallback/order-service"))
						)
						.uri("lb://ORDER-SERVICE"))
				.build();
	}
}