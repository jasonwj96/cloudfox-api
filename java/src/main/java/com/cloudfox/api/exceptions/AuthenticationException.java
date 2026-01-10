package com.cloudfox.api.exceptions;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException() {
        super("Authentication error.");
    }
}