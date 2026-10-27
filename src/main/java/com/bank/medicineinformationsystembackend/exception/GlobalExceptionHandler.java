package com.bank.medicineinformationsystembackend.exception;

import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskRejectedException.class)
    public ResponseEntity<String> handleTaskRejectedException(TaskRejectedException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Сервер тимчасово перевантажений, спробуйте пізніше.");
    }
}