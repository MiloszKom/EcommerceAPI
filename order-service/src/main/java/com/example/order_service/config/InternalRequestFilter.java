package com.example.order_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class InternalRequestFilter implements Filter {

    private static final String HEADER_NAME = "X-Internal-Secret";

    @Value("${app.gateway.secret}")
    private String secretValue;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String header = httpRequest.getHeader(HEADER_NAME);

        if (!secretValue.equals(header)) {
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Access denied: this service can only be accessed via the API Gateway");
            responseBody.put("status", 403);

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(responseBody);

            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(json);
            httpResponse.getWriter().flush();
            return;
        }

        chain.doFilter(request, response);
    }

    @Bean
    public FilterRegistrationBean<InternalRequestFilter> filterRegistrationBean() {
        FilterRegistrationBean<InternalRequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(this);
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}