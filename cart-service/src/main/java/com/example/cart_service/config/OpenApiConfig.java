package com.example.cart_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition(
        security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "oauth2")
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(
            @Value("${EXTERNAL_KEYCLOAK_URL}") String keycloakBase,
            @Value("${API_GATEWAY_URL}") String apiGatewayUrl
    ) {
        String authUrl = keycloakBase + "/realms/ecommerce-realm/protocol/openid-connect/auth";
        String tokenUrl = keycloakBase + "/realms/ecommerce-realm/protocol/openid-connect/token";

        return new OpenAPI()
                .info(new Info()
                        .title("Cart Service API")
                        .version("1.0")
                        .description("Cart Service for Ecommerce Application")
                )
                .servers(List.of(
                        new Server()
                                .url(apiGatewayUrl)
                                .description("API Gateway")
                ))
                .addSecurityItem(new SecurityRequirement().addList("oauth2"))
                .components(new Components()
                        .addSecuritySchemes("oauth2", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .flows(new OAuthFlows()
                                        .authorizationCode(new OAuthFlow()
                                                .authorizationUrl(authUrl)
                                                .tokenUrl(tokenUrl)
                                                .scopes(new Scopes()
                                                        .addString("openid", "OpenID Connect")
                                                        .addString("profile", "User Profile")
                                                        .addString("email", "Email")
                                                )
                                        )
                                )
                        )
                );
    }
}