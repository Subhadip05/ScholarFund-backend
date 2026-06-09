package com.scholarfund.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ScholarFundException.class)
    public ResponseEntity<Map<String, Object>> handleScholarFundException(ScholarFundException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error_code", ex.getErrorCode().name());
        errorResponse.put("message", ex.getMessage());

        // Map common errors to appropriate HTTP statuses
        HttpStatus status = switch (ex.getErrorCode()) {
            case NOT_FOUND, USER_NOT_FOUND, FILE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case UNAUTHORIZED, INVALID_TOKEN, EXPIRED_TOKEN, EXPIRED_REFRESH_TOKEN, INVALID_OTP -> HttpStatus.UNAUTHORIZED;
            case ALREADY_EXIST -> HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return new ResponseEntity<>(errorResponse, status);
    }
}