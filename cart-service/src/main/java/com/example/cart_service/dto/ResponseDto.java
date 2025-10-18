package com.example.cart_service.dto;

public class ResponseDto<T> {
    private int statusCode;
    private String message;
    private T data;

    public ResponseDto() {}

    public ResponseDto(int statusCode, String message, T data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
