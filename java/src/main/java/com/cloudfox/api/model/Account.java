package com.cloudfox.api.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cfx_accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "fullname", nullable = false)
    private String fullname;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "password_salt", nullable = false)
    private byte[] passwordSalt;

    @Column(name = "password_hash_algo", nullable = false)
    private String passwordHashAlgo;

    @Column(name = "email")
    private String email;

    @Column(name = "mfa_enabled", nullable = false)
    private boolean mfaEnabled;

    @Column(name = "mfa_type", nullable = false)
    private String mfaType;

    @Column(name = "creation_date", nullable = false)
    private Instant creationDate;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "active", nullable = false)
    private boolean active;
}