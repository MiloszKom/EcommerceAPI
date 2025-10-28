package com.example.order_service.exception;

import com.example.order_service.dto.ErrorResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Utility method to get request path
    private String getRequestPath(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRequestURI();
        }
        return "Unknown path";
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, String> validationErrors = new HashMap<>();
        List<ObjectError> validationErrorList = ex.getBindingResult().getAllErrors();

        validationErrorList.forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String validationMsg = error.getDefaultMessage();
            validationErrors.put(fieldName, validationMsg);
        });

        String requestPath = getRequestPath(request);
        log.warn("Validation failed for request [{}] - {} fields invalid: {}",
                requestPath, validationErrors.size(), validationErrors);

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "One or more fields have invalid values",
                LocalDateTime.now(),
                requestPath,
                validationErrors
        );

        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(
            NotFoundException exception,
            WebRequest webRequest
    ) {
        String requestPath = getRequestPath(webRequest);
        log.warn("Resource not found: {} at [{}]", exception.getMessage(), requestPath);

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                requestPath
        );

        return new ResponseEntity<>(errorResponseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(
            IllegalArgumentException exception,
            WebRequest webRequest
    ) {
        String requestPath = getRequestPath(webRequest);
        log.warn("Invalid argument: {} at [{}]", exception.getMessage(), requestPath);

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                requestPath
        );

        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDto> handleConflictException(
            ConflictException exception,
            WebRequest webRequest
    ) {
        String requestPath = getRequestPath(webRequest);
        log.warn("Conflict: {} at [{}]", exception.getMessage(), requestPath);

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                requestPath
        );

        return new ResponseEntity<>(errorResponseDto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(
            AccessDeniedException exception,
            WebRequest webRequest
    ) {
        String requestPath = getRequestPath(webRequest);
        log.warn("Access denied: {} at [{}]", exception.getMessage(), requestPath);

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                HttpStatus.FORBIDDEN.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                requestPath
        );

        return new ResponseEntity<>(errorResponseDto, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponseDto> handleExternalServiceException(
            ExternalServiceException ex,
            WebRequest request
    ) {
        String requestPath = getRequestPath(request);
        log.error("External service error: {} at [{}]", ex.getMessage(), requestPath, ex);

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                ex.getStatus().value(),
                ex.getMessage(),
                LocalDateTime.now(),
                requestPath
        );
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<ErrorResponseDto> handleAuthenticationException(InsufficientAuthenticationException ex, WebRequest request) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication is required to access this resource.",
                LocalDateTime.now(),
                getRequestPath(request)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidBearerTokenException.class)
    public ResponseEntity<ErrorResponseDto> handleJwtException(InvalidBearerTokenException ex, WebRequest request) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.UNAUTHORIZED.value(),
                "The access token provided is expired, revoked, malformed, or invalid for other reasons.",
                LocalDateTime.now(),
                getRequestPath(request)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex, WebRequest request) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.FORBIDDEN.value(),
                "Access denied: You do not have the required privileges to access this resource.",
                LocalDateTime.now(),
                getRequestPath(request)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGlobalException(
            Exception exception,
            WebRequest webRequest
    ) {
        String requestPath = getRequestPath(webRequest);
        log.error("Unexpected error: {} at [{}]", exception.getMessage(), requestPath, exception);

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                LocalDateTime.now(),
                requestPath
        );

        return new ResponseEntity<>(errorResponseDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

