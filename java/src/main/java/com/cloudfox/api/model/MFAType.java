package com.cloudfox.api.model;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MFAType {
    BCRYPT("bcrypt");

    private final String value;
}