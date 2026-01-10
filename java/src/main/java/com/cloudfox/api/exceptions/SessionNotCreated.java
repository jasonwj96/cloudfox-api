package com.cloudfox.api.exceptions;

public class SessionNotCreated extends RuntimeException {
    public SessionNotCreated() {
        super("Session not created.");
    }
}
