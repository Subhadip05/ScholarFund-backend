package com.scholarfund.backend.common.exception;

import com.scholarfund.backend.common.model.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ScholarFundException.class)
    public ResponseEntity<ApiResponse<Object>> handleScholarFundException(ScholarFundException ex) {
        log.warn("Business Exception: {}", ex.getMessage());

        // Map custom ErrorCode enum to an actual HTTP Status
        HttpStatus status = mapErrorCodeToHttpStatus(ex.getErrorCode());

        ApiResponse<Object> response = new ApiResponse<>(
                status.value(),
                ex.getErrorCode().name(),
                ex.getMessage(),
                null
        );

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(Exception ex) {
        log.error("Unexpected Error: ", ex);

        ApiResponse<Object> response = new ApiResponse<>(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again later.",
                null
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private HttpStatus mapErrorCodeToHttpStatus(ErrorCode errorCode) {
        if (errorCode == null) return HttpStatus.INTERNAL_SERVER_ERROR;

        return switch (errorCode) {
            case NOT_FOUND, USER_NOT_FOUND, FILE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case UNAUTHORIZED, INVALID_TOKEN, EXPIRED_TOKEN, EXPIRED_REFRESH_TOKEN, INVALID_OTP -> HttpStatus.UNAUTHORIZED;
            case ALREADY_EXIST -> HttpStatus.CONFLICT;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}