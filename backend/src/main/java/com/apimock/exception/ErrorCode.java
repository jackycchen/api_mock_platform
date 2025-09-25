package com.apimock.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    VALIDATION_ERROR(40001, HttpStatus.BAD_REQUEST, "参数校验失败"),
    AUTHENTICATION_FAILED(40100, HttpStatus.UNAUTHORIZED, "认证失败"),
    ACCOUNT_LOCKED(40101, HttpStatus.LOCKED, "账户已被锁定"),
    ACCESS_DENIED(40300, HttpStatus.FORBIDDEN, "无权限执行此操作"),
    RESOURCE_NOT_FOUND(40400, HttpStatus.NOT_FOUND, "资源不存在"),
    PROJECT_NOT_FOUND(40410, HttpStatus.NOT_FOUND, "项目不存在"),
    API_NOT_FOUND(40420, HttpStatus.NOT_FOUND, "API接口不存在"),
    CONFLICT_ERROR(40900, HttpStatus.CONFLICT, "资源冲突"),
    RATE_LIMITED(42900, HttpStatus.TOO_MANY_REQUESTS, "请求过于频繁"),
    INTERNAL_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "系统内部错误");

    private final int code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(int code, HttpStatus httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public int getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
