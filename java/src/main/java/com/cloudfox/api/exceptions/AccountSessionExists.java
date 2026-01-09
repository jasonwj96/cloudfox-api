package com.cloudfox.api.exceptions;

public class AccountSessionExists extends RuntimeException {
    public AccountSessionExists() {
        super("This account is already logged in.");
    }
}
