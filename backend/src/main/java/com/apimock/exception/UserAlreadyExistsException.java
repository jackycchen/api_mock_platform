package com.apimock.exception;

public class UserAlreadyExistsException extends RuntimeException {

    private String field; // username 或 email

    public UserAlreadyExistsException(String message, String field) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}