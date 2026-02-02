package com.cloudfox.api.exceptions;

public class AccountNotFound extends RuntimeException {
    public AccountNotFound() {
        super("Account does not exist.");
    }
}
