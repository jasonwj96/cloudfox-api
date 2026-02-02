package com.cloudfox.api.exceptions;

public class IdempotencyReplayException extends RuntimeException {
    public IdempotencyReplayException(String message) {
        super(message);
    }
}
