package com.bank.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppointmentCircuitBreakerOpenException.class)
    public ProblemDetail handleCircuitBreakerOpen(AppointmentCircuitBreakerOpenException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                ex.getMessage()
        );
        problem.setTitle("Appointment Service Circuit Breaker Open");
        problem.setProperty("reason", "CIRCUIT_BREAKER_OPEN");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(RemoteResourceNotFoundException.class)
    public ProblemDetail handleRemoteNotFound(RemoteResourceNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Remote Resource Not Found");
        return problem;
    }

    @ExceptionHandler(RemoteServiceUnavailableException.class)
    public ProblemDetail handleRemoteUnavailable(RemoteServiceUnavailableException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
        problem.setTitle("Remote Service Unavailable");
        return problem;
    }
}