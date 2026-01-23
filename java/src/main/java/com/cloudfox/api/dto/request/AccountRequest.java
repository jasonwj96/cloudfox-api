package com.cloudfox.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountRequest {

    @Size(min = 3, max = 32)
    private String username;

    @Size(min = 1, max = 255)
    private String fullname;

    @Email
    @Size(min = 3, max = 255)
    private String email;

    @Size(min = 12, max = 64)
    private String password;
}