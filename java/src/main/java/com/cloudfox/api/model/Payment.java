package com.cloudfox.api.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cfx_payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false)
    private UUID publicId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "amount_lowest_unit", nullable = false)
    private int amountLowestUnit;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "creation_date", nullable = false)
    private Instant creationDate;
}