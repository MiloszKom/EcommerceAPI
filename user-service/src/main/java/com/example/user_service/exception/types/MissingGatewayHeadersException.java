package com.example.user_service.exception.types;

public class MissingGatewayHeadersException extends RuntimeException {
    public MissingGatewayHeadersException(String message) {
        super(message);
    }
}
