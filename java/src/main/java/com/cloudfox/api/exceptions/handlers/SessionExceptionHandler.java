package com.cloudfox.api.exceptions.handlers;

import com.cloudfox.api.exceptions.AccountAlreadyExists;
import com.cloudfox.api.exceptions.AccountSessionExists;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class SessionExceptionHandler {
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

}