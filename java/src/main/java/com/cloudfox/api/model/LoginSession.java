package com.cloudfox.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cfx_session")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginSession {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "session_token", nullable = false)
    private UUID sessionToken;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "creation_date", updatable = false)
    private Instant creationDate;

    @Column(name = "expiration_date", nullable = false)
    private Instant expirationDate;

    @Column(name = "last_active_date")
    private Instant lastActiveDate;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}