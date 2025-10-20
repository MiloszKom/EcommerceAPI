package com.example.user_service.exception;

import com.example.user_service.dto.ErrorResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
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

    // Utility method to get cleaned request details
    private String getRequestDetails(WebRequest webRequest) {
        String uri = webRequest.getDescription(false).replace("uri=", "");
        String httpMethod = (webRequest instanceof ServletWebRequest)
                ? ((ServletWebRequest) webRequest).getRequest().getMethod()
                : "UNKNOWN";
        return String.format("%s %s", httpMethod, uri);
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

        String requestDetails = getRequestDetails(request);
        log.warn("Validation failed for request [{}] - {} fields invalid: {}",
                requestDetails, validationErrors.size(), validationErrors);

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "One or more fields have invalid values",
                LocalDateTime.now(),
                requestDetails,
                validationErrors
        );

        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(
            ResourceNotFoundException exception,
            WebRequest webRequest
    ) {
        String requestDetails = getRequestDetails(webRequest);
        log.warn("Resource not found: {} at [{}]", exception.getMessage(), requestDetails);

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                requestDetails
        );

        return new ResponseEntity<>(errorResponseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrityViolationException(
            DataIntegrityViolationException exception,
            WebRequest webRequest
    ) {
        String message = "Database constraint violated";
        Throwable rootCause = exception.getMostSpecificCause();
        String rootMsg = rootCause.getMessage();
        if (rootMsg != null && rootMsg.toLowerCase().contains("email")) {
            message = "User already registered with this email";
        }

        String requestDetails = getRequestDetails(webRequest);
        log.error("Data integrity violation at [{}] - root cause: {}", requestDetails, rootMsg, exception);

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                HttpStatus.CONFLICT.value(),
                message,
                LocalDateTime.now(),
                requestDetails
        );

        return new ResponseEntity<>(errorResponseDto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleUserAlreadyExists(
            UserAlreadyExistsException ex,
            WebRequest request
    ) {
        String requestDetails = getRequestDetails(request);
        log.warn("User registration conflict: {} at [{}]", ex.getMessage(), requestDetails);

        ErrorResponseDto dto = new ErrorResponseDto(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                LocalDateTime.now(),
                requestDetails
        );
        return new ResponseEntity<>(dto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(KeycloakException.class)
    public ResponseEntity<ErrorResponseDto> handleKeycloakException(
            KeycloakException ex,
            WebRequest request
    ) {
        HttpStatus status = ex.getStatus() != null ? ex.getStatus() : HttpStatus.BAD_GATEWAY;
        String message;
        String requestDetails = getRequestDetails(request);

        if (status.is5xxServerError()) {
            message = "Internal server error";
            log.error("Keycloak server error ({}): {} at [{}]", status, ex.getMessage(), requestDetails, ex);
        } else {
            message = ex.getMessage() != null && !ex.getMessage().isEmpty()
                    ? ex.getMessage()
                    : "Authentication service error";
            log.warn("Keycloak client error ({}): {} at [{}]", status, ex.getMessage(), requestDetails);
        }

        ErrorResponseDto dto = new ErrorResponseDto(
                status.value(),
                message,
                LocalDateTime.now(),
                requestDetails
        );
        return new ResponseEntity<>(dto, status);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingRequestHeaderException(
            MissingRequestHeaderException ex,
            WebRequest request
    ) {
        String requestDetails = getRequestDetails(request);
        String message = "Missing required header: " + ex.getHeaderName();
        log.warn("Missing header: {} at [{}]", message, requestDetails);

        ErrorResponseDto dto = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                message,
                LocalDateTime.now(),
                requestDetails
        );
        return new ResponseEntity<>(dto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            WebRequest request
    ) {
        String requestDetails = getRequestDetails(request);
        log.error("Unexpected error: {} at [{}]", ex.getMessage(), requestDetails, ex);

        ErrorResponseDto dto = new ErrorResponseDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                LocalDateTime.now(),
                requestDetails
        );
        return new ResponseEntity<>(dto, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
