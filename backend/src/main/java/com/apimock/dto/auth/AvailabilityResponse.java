package com.apimock.dto.auth;

public class AvailabilityResponse {

    private boolean available;
    private String field; // username 或 email

    // Constructors
    public AvailabilityResponse() {}

    public AvailabilityResponse(boolean available, String field) {
        this.available = available;
        this.field = field;
    }

    // Getters and Setters
    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}