package com.cloudfox.api.exceptions;

public class ModelDoesNotExist extends RuntimeException {
    public ModelDoesNotExist() {
        super("The model ID provided does not exist.");
    }
}
