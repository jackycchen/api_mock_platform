package com.apimock.exception;

public class ApiPathConflictException extends RuntimeException {
    public ApiPathConflictException(String message) {
        super(message);
    }

    public ApiPathConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}