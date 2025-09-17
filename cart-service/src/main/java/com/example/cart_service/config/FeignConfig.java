package com.example.cart_service.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        try {
            String token = SecurityUtils.getCurrentUserToken();
            template.header("Authorization", "Bearer " + token);
        } catch (RuntimeException e) {
            System.out.println("No JWT token found for Feign request: " + e.getMessage());
        }
    }
}
