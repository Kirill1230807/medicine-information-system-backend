package com.bank.userservice.exception;

public class AppointmentCircuitBreakerOpenException extends RuntimeException {
    public AppointmentCircuitBreakerOpenException(String message, Throwable cause) {
        super(message, cause);
    }
}