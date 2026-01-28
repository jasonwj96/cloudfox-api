package com.cloudfox.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "cfx_pricing_plans")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PricingPlan {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Positive
    @Column(name = "price_micros", nullable = false)
    private long priceMicros;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency", nullable = false)
    private Currency currency;
}