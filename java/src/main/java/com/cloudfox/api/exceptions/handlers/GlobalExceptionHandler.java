package com.cloudfox.api.exceptions.handlers;

import com.cloudfox.api.exceptions.AccountAlreadyExists;
import com.cloudfox.api.exceptions.AccountSessionExists;
import com.cloudfox.api.exceptions.SessionNotCreated;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "status", 500,
                        "error", "Internal Server Error",
                        "message", "An unexpected error occurred",
                        "code", "ERR_INTERNAL"
                ));
    }

    @ExceptionHandler(AccountAlreadyExists .class)
    public ResponseEntity<Map<String, Object>> handleAccountExists(AccountAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "status", 409,
                        "error", "Conflict",
                        "message", ex.getMessage(),
                        "code", "ERR_ACCOUNT_DUPLICATE"
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            if (fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage()) != null) {
                throw new IllegalStateException("Duplicate key");
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "status", 400,
                        "errors", fieldErrors,
                        "code", "ERR_VALIDATION_FAILED"
                ));
    }

    @ExceptionHandler(AccountSessionExists.class)
    public ResponseEntity<Map<String, Object>> handleAccountAlreadyLoggedIn(AccountAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "status", 409,
                        "error", "Conflict",
                        "message", ex.getMessage(),
                        "code", "ERR_ACCOUNT_LOGGED_IN"
                ));
    }

    @ExceptionHandler(SessionNotCreated.class)
    public ResponseEntity<Map<String, Object>> handleSessionNotCreated(SessionNotCreated ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "status", 401,
                        "error", "Unauthorized",
                        "message", ex.getMessage(),
                        "code", "ERR_SESSION_NOT_CREATED"
                ));
    }
}