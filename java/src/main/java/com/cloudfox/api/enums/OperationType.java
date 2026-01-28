package com.cloudfox.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OperationType {
    PAYMENT("PAYMENT");

    private final String value;
}
