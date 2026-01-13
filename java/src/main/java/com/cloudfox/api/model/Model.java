package com.cloudfox.api.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cfx_models")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "generated_tokens", nullable = false)
    private int generatedTokens;

    @CreationTimestamp
    @Column(name = "creation_date", nullable = false)
    private Instant creationDate;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "framework", nullable = false)
    private String framework;

    @Column(name = "active", nullable = false)
    private boolean active;

    @UpdateTimestamp
    @Column(name = "last_modified", nullable = false)
    private Instant lastModified;
}