package com.cloudfox.api.repository;

import com.cloudfox.api.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPublicId(java.util.UUID publicId);

    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
}