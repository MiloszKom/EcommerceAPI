package com.example.cart_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDto {

    private final int statusCode;
    private final String message;
    private final LocalDateTime timestamp;
    private final String path;

    // Keep this for validation errors (field -> message)
    private final Map<String, String> errors;

    // New property for stack trace, exception type, etc.
    private Map<String, Object> details;

    // Constructor without errors/details
    public ErrorResponseDto(int statusCode, String message, LocalDateTime timestamp, String path) {
        this(statusCode, message, timestamp, path, null);
    }

    // Constructor with errors
    public ErrorResponseDto(int statusCode, String message, LocalDateTime timestamp, String path, Map<String, String> errors) {
        this.statusCode = statusCode;
        this.message = message;
        this.timestamp = timestamp;
        this.path = path;
        this.errors = errors;
    }

    // Getters
    public int getStatusCode() { return statusCode; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getPath() { return path; }
    public Map<String, String> getErrors() { return errors; }
    public Map<String, Object> getDetails() { return details; }

    // Setter for details (optional, only used in global exception handler)
    public void setDetails(Map<String, Object> details) { this.details = details; }
}
