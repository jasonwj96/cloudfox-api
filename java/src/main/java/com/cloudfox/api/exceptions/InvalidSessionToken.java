package com.cloudfox.api.exceptions;

public class InvalidSessionToken extends RuntimeException {
    public InvalidSessionToken() {
        super("Invalid session token.");
    }
}
