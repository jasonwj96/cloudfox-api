package com.cloudfox.api.dto.request;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SessionRequest {
    private String username;
    private String password;
    private String userAgent;
    private String ipAddress;
    private Instant expirationDate;
    private UUID sessionToken;
}