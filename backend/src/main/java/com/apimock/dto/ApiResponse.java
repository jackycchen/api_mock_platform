package com.apimock.dto;

import com.apimock.exception.ErrorCode;

import java.time.LocalDateTime;

public class ApiResponse<T> {

    private Integer code;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private boolean success;

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(Integer code, String message) {
        this();
        this.code = code;
        this.message = message;
        this.success = code != null && code < 400;
    }

    public ApiResponse(Integer code, String message, T data) {
        this(code, message);
        this.data = data;
    }

    // 成功响应
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>(200, "success", data);
        response.setSuccess(true);
        return response;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>(200, message, data);
        response.setSuccess(true);
        return response;
    }

    public static ApiResponse<Void> success() {
        ApiResponse<Void> response = new ApiResponse<>(200, "success");
        response.setSuccess(true);
        return response;
    }

    public static ApiResponse<Void> successMessage(String message) {
        ApiResponse<Void> response = new ApiResponse<>(200, message);
        response.setSuccess(true);
        return response;
    }

    // 错误响应
    public static <T> ApiResponse<T> error(Integer code, String message) {
        ApiResponse<T> response = new ApiResponse<>(code, message);
        response.setSuccess(false);
        return response;
    }

    public static <T> ApiResponse<T> error(Integer code, String message, T data) {
        ApiResponse<T> response = new ApiResponse<>(code, message, data);
        response.setSuccess(false);
        return response;
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return error(errorCode, errorCode.getDefaultMessage(), null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return error(errorCode, message, null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message, T data) {
        ApiResponse<T> response = new ApiResponse<>(errorCode.getCode(), message, data);
        response.setSuccess(false);
        return response;
    }

    // Getters and Setters
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
