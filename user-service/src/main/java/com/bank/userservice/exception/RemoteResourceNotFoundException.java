package com.bank.userservice.exception;

public class RemoteResourceNotFoundException extends RuntimeException {
    public RemoteResourceNotFoundException(String message) {
        super(message);
    }
}