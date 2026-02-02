package com.cloudfox.api.model;

import com.cloudfox.api.enums.PaymentStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cfx_payments")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, updatable = false)
    private UUID publicId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "amount_lowest_unit", nullable = false)
    private long amountLowestUnit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency", nullable = false)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatusEnum status;

    @Column(name = "provider")
    private String provider;

    @CreationTimestamp
    @Column(name = "creation_date", nullable = false, updatable = false)
    private Instant creationDate;

    @Column(name = "idempotent_key_id", unique = true)
    private String idempotentKeyId;

    @Column(name = "provider_payment_id", nullable = false, unique = true)
    private String providerPaymentId;

    @Column(name = "token_amount", nullable = false)
    private long tokenAmount;

    @PrePersist
    void onCreate() {
        this.publicId = UUID.randomUUID();
    }
}