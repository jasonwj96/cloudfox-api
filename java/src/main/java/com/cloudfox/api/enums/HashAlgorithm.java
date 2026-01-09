package com.cloudfox.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum HashAlgorithm {
    BCRYPT("bcrypt"),
    ARGON2("argon2");

    private String value;
}
