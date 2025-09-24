package com.example.order_service.config;

import com.example.order_service.exception.types.RemoteServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;

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

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    static class CustomErrorDecoder implements ErrorDecoder {
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public Exception decode(String methodKey, Response response) {
            int status = response.status();
            String message = "Error from product-service [" + status + "]";

            try {
                if (response.body() != null) {
                    String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));

                    // Try to parse the error response JSON
                    JsonNode json = objectMapper.readTree(body);
                    if (json.has("message")) {
                        message = json.get("message").asText();
                    }
                }
            } catch (Exception ignored) {}

            return new RemoteServiceException("product-service", HttpStatus.valueOf(status), message);
        }
    }
}
