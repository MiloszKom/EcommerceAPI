package com.example.user_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDto {

    private final int statusCode;
    private final String message;
    private final LocalDateTime timestamp;
    private final String path;
    private final Map<String, String> errors;

    public ErrorResponseDto(int statusCode, String message, LocalDateTime timestamp, String path) {
        this(statusCode, message, timestamp, path, null);
    }

    public ErrorResponseDto(int statusCode, String message, LocalDateTime timestamp, String path, Map<String, String> errors) {
        this.statusCode = statusCode;
        this.message = message;
        this.timestamp = timestamp;
        this.path = path;
        this.errors = errors;
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

    public Map<String, String> getErrors() {
        return errors;
    }
}
