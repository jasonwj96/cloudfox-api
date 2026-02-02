package com.cloudfox.api.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MFATypeEnum {
    TEST("test");

    private final String value;
}