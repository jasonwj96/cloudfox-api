package com.cloudfox.api.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MFAType {
    BCRYPT("bcrypt");

    private final String value;
}