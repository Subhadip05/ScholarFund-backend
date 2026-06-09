package com.scholarfund.backend.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private int status;
    private String code;
    private String message;
    private T data;
    private T data2;
    private int recordCount;

    public ApiResponse(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = null;
    }

    public ApiResponse(int status, String code, String message, T result) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = result;
    }

    public ApiResponse(int status, String code, String message, T result, T result2) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = result;
        this.data2 = result2;
    }

    public ApiResponse(int status, String code, String message, T result, int count) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = result;
        this.recordCount = count;
    }
}