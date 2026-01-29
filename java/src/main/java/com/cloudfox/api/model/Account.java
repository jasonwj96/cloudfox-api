package com.cloudfox.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "cfx_accounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "username", length = 32, nullable = false, unique = true)
    private String username;

    @Column(name = "fullname", length = 50, nullable = false)
    private String fullname;

    @Column(name = "password_hash", nullable = false, unique = true)
    private String passwordHash;

    @Column(name = "password_salt", unique = true)
    private byte[] passwordSalt;

    @Column(name = "password_hash_algo", nullable = false)
    private String passwordHashAlgo;

    @Column(name="email", length = 50, unique = true)
    private String email;

    @Column(name = "mfa_enabled")
    private boolean mfaEnabled = false;

    @Column(name = "mfa_type", length = 30)
    private String mfaType;

    @Column(name = "creation_date")
    private OffsetDateTime creationDate;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "active", nullable = false)
    private boolean active = false;

    @Column(name = "token_balance")
    private long tokenBalance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pricing_plan", nullable = false)
    private PricingPlan pricingPlan;

}
