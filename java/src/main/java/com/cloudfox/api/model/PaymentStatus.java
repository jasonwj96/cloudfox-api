package com.cloudfox.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentStatus {
    PENDING("pending"),
    SUCCEEDED("succeeded"),
    FAILED("failed");

    private final String value;
}