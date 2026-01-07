package com.cloudfox.api.model;


import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cfx_models")
public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "generated_tokens", nullable = false)
    private int generatedTokens;

    @Column(name = "creation_date", nullable = false)
    private Instant creationDate;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "last_modified", nullable = false)
    private Instant lastModified;
}