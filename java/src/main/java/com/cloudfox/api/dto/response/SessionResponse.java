package com.cloudfox.api.dto.response;

import com.cloudfox.api.model.Account;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SessionResponse {
    private Instant expirationDate;
}
