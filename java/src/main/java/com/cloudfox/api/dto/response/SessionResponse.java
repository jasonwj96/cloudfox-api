package com.cloudfox.api.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SessionResponse {
    private UUID sessionToken;
}
