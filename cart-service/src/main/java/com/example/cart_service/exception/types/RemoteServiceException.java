package com.example.cart_service.exception.types;

import org.springframework.http.HttpStatus;

public class RemoteServiceException extends RuntimeException {
    private final HttpStatus statusCode;
    private final String serviceName;

    public RemoteServiceException(String serviceName, HttpStatus statusCode, String message) {
        super(message);
        this.serviceName = serviceName;
        this.statusCode = statusCode;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    public String getServiceName() {
        return serviceName;
    }
}
