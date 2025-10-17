package com.example.user_service.exception;

import org.springframework.http.HttpStatus;

public class KeycloakException extends RuntimeException {
    private final HttpStatus status;

    public KeycloakException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
