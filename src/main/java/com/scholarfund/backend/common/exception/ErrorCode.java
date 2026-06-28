package com.scholarfund.backend.common.exception;

public enum ErrorCode {
    // General Errors
    GENERAL,
    NOT_FOUND,
    INTERNAL_SERVER_ERROR,
    ALREADY_EXIST,
    NOT_SAVED,

    // Authentication & Security
    UNAUTHORIZED,
    NOT_LOGGED_IN,
    INVALID_TOKEN,
    EXPIRED_TOKEN,
    EXPIRED_REFRESH_TOKEN,
    INVALID_OTP,

    // File Handling
    FILE_NOT_FOUND,
    INVALID_FILE_NAME,
    INVALID_EXCEL_FILE,
    FILE_COULD_NOT_BE_STORED,
    ERROR_CREATING_DIRECTORY,
    ERROR_COPYING_FILE,
    ERROR_READING_FILE,
    ERROR_UPLOADING_FILE,
    ERROR_GENERATING_REPORT,
    EMPTY_FILE_REQUEST,

    // ScholarFund Specific
    USER_NOT_FOUND,
    INVALID_LOAN_STATUS,
    BAD_REQUEST
}