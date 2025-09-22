package com.example.user_service.config;

import jakarta.servlet.http.HttpServletRequest;

public class SecurityUtils {

    public static Long getCurrentUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader("userId");
        if (userIdHeader == null) {
            throw new RuntimeException("No userId header found");
        }
        return Long.parseLong(userIdHeader);
    }

}
