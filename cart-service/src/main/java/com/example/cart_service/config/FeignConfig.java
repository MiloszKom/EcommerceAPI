package com.example.cart_service.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class FeignConfig implements RequestInterceptor {

    @Value("${app.gateway.secret}")
    private String internalSecret;

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();

            String userId = request.getHeader("userId");
            String role = request.getHeader("role");
            String secretHeader = request.getHeader("X-Internal-Secret");

            if (userId != null) {
                template.header("userId", userId);
            }
            if (role != null) {
                template.header("role", role);
            }
            if (secretHeader != null) {
                template.header("X-Internal-Secret", secretHeader);
            }
        } else {
            template.header("X-Internal-Secret", internalSecret);
        }
    }
}
