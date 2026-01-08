package com.cloudfox.api.exceptions;

public class AccountAlreadyExists extends RuntimeException {
    public AccountAlreadyExists() {
        super("An account with that username already exists.");
    }
}