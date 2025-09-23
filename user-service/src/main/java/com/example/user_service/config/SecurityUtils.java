package com.example.user_service.config;

import com.example.user_service.exception.types.MissingGatewayHeadersException;
import jakarta.servlet.http.HttpServletRequest;

public class SecurityUtils {

    public static Long getCurrentUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader("userId");
        if (userIdHeader == null) {
            throw new MissingGatewayHeadersException("Missing required header from API Gateway");
        }
        return Long.parseLong(userIdHeader);
    }

    public static String getCurrentUserRole(HttpServletRequest request) {
        String roleHeader = request.getHeader("role");
        if (roleHeader == null) {
            throw new MissingGatewayHeadersException("Missing required header from API Gateway");
        }
        return roleHeader;
    }

}
