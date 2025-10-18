package com.example.gatewayserver.security;

import java.time.LocalDateTime;

public class ErrorResponseDto {

    private final int statusCode;
    private final String message;
    private final LocalDateTime timestamp;
    private final String path;

    public ErrorResponseDto(int statusCode, String message, LocalDateTime timestamp, String path) {
        this.statusCode = statusCode;
        this.message = message;
        this.timestamp = timestamp;
        this.path = path;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getPath() {
        return path;
    }
}
