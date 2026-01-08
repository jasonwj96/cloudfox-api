package com.cloudfox.api.model;

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
