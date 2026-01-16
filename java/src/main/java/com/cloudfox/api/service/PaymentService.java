package com.cloudfox.api.service;

import com.cloudfox.api.dto.request.PaymentRequest;
import com.cloudfox.api.dto.response.PaymentResponse;
import com.cloudfox.api.exceptions.IdempotencyReplayException;
import com.cloudfox.api.model.*;
import com.cloudfox.api.repository.AccountRepository;
import com.cloudfox.api.repository.IdempotentOperationRepository;
import com.cloudfox.api.repository.PaymentRepository;
import com.cloudfox.api.repository.SessionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private static final String OPERATION_CREATE_PAYMENT = "CREATE_PAYMENT";

    private final PaymentRepository paymentRepository;
    private final IdempotentOperationRepository idempotentRepository;
    private final SessionRepository sessionRepository;
    private final AccountRepository accountRepository;
    private final ObjectMapper objectMapper;

    public PaymentResponse createPaymentIntent(
            UUID sessionToken,
            PaymentRequest request
    ) {

        LoginSession session = sessionRepository
                .findBySessionTokenAndIsActiveTrueAndExpirationDateAfter(
                        sessionToken, Instant.now())
                .orElseThrow(() ->
                        new SessionAuthenticationException("Invalid session"));

        Account account = accountRepository.findById(session.getAccountId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Account not found"));

        Optional<IdempotentOperation> existing =
                idempotentRepository.findByIdempotencyKeyAndOperation(
                        request.idempotencyKey(),
                        OPERATION_CREATE_PAYMENT
                );

        if (existing.isPresent()) {
            return deserialize(existing.get().getResponseBody());
        }

        long amountLowestUnit = request.amountLowestUnit();

        if (amountLowestUnit <= 0) {
            throw new IllegalArgumentException("Invalid amount.");
        }

        if (request.idempotencyKey() == null || request.idempotencyKey().isBlank()) {
            throw new IllegalArgumentException("Missing idempotency key");
        }

        String currency = request.currency();

        PaymentIntent intent;

        try {
            intent = PaymentIntent.create(
                    PaymentIntentCreateParams.builder()
                            .setAmount(amountLowestUnit)
                            .setCurrency(currency)
                            .putMetadata("account_id", String.valueOf(account.getId()))
                            .build()
            );
        } catch (StripeException e) {
            throw new RuntimeException("Stripe PaymentIntent creation failed", e);
        }

        Payment payment = Payment.builder()
                .accountId(account.getId())
                .amountLowestUnit(amountLowestUnit)
                .currency(currency)
                .status(PaymentStatus.pending)
                .stripePaymentIntentId(intent.getId())
                .build();

        paymentRepository.save(payment);

        PaymentResponse response =
                new PaymentResponse(intent.getClientSecret());

        storeIdempotentResponse(request.idempotencyKey(), response);

        return response;
    }

    private void storeIdempotentResponse(
            String idempotencyKey,
            PaymentResponse response
    ) {
        try {
            IdempotentOperation idp = IdempotentOperation.builder()
                    .idempotencyKey(idempotencyKey)
                    .operation(OPERATION_CREATE_PAYMENT)
                    .requestHash("na")
                    .responseStatus(200)
                    .responseBody(objectMapper.writeValueAsString(response))
                    .expiresAt(OffsetDateTime.now().plusHours(24))
                    .build();


            idempotentRepository.save(idp);

        } catch (DataIntegrityViolationException e) {
            IdempotentOperation existing =
                    idempotentRepository
                            .findByIdempotencyKeyAndOperation(
                                    idempotencyKey, OPERATION_CREATE_PAYMENT
                            )
                            .orElseThrow();

            throw new IdempotencyReplayException(existing.getResponseBody());

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private PaymentResponse deserialize(String json) {
        try {
            return objectMapper.readValue(json, PaymentResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize cached response", e);
        }
    }
}
