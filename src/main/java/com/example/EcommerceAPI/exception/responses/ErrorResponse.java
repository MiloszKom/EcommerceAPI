package com.example.EcommerceAPI.exception.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public class ErrorResponse {
    private final String message;
    private final int status;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<FieldValidationError> errors;

    public ErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
        this.errors = null;
    }

    public ErrorResponse(String message, int status, List<FieldValidationError> errors) {
        this.message = message;
        this.status = status;
        this.errors = errors;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public List<FieldValidationError> getErrors() { return errors ; }

    public static class FieldValidationError {
        private final String field;
        private final String message;

        public FieldValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }
    }
}