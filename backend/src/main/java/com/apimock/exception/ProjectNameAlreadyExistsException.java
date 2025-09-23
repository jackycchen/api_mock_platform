package com.apimock.exception;

public class ProjectNameAlreadyExistsException extends RuntimeException {
    public ProjectNameAlreadyExistsException(String message) {
        super(message);
    }

    public ProjectNameAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}