package com.scholarfund.backend.common.exception;

import lombok.Getter;

@Getter
public class ScholarFundException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object[] params;

    public ScholarFundException(ErrorCode errorCode, Object[] params) {
        this.errorCode = errorCode;
        this.params = params;
    }

    public ScholarFundException(String message, ErrorCode errorCode, Object[] params) {
        super(message);
        this.errorCode = errorCode;
        this.params = params;
    }

    public ScholarFundException(String message, Throwable cause, ErrorCode errorCode, Object[] params) {
        super(message, cause);
        this.errorCode = errorCode;
        this.params = params;
    }

    public ScholarFundException(Throwable cause, ErrorCode errorCode, Object[] params) {
        super(cause);
        this.errorCode = errorCode;
        this.params = params;
    }

    public ScholarFundException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace,
            ErrorCode errorCode,
            Object[] params) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errorCode = errorCode;
        this.params = params;
    }

}