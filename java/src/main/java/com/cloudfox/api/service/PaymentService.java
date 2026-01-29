package com.cloudfox.api.service;

import com.cloudfox.api.dto.request.PaymentRequest;
import com.cloudfox.api.dto.response.PaymentResponse;
import com.cloudfox.api.enums.OperationTypeEnum;
import com.cloudfox.api.enums.PaymentStatusEnum;
import com.cloudfox.api.model.Account;
import com.cloudfox.api.model.IdempotentOperation;
import com.cloudfox.api.model.Payment;
import com.cloudfox.api.model.PricingPlan;
import com.cloudfox.api.repository.AccountRepository;
import com.cloudfox.api.repository.IdempotentOperationRepository;
import com.cloudfox.api.repository.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final IdempotentOperationRepository idempotentRepository;
    private final ObjectMapper objectMapper;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    public PaymentResponse createPaymentIntent(
            UUID accountId,
            PaymentRequest request) {

        Optional<IdempotentOperation> cached =
                idempotentRepository.findByIdempotencyKeyAndOperation(
                        request.idempotencyKey(),
                        OperationTypeEnum.PAYMENT.getValue());

        if (cached.isPresent()) {
            return deserialize(cached.get().getResponseBody());
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalStateException("Account not found"));

        PricingPlan plan = account.getPricingPlan();

        long amountLowestUnit = Math.multiplyExact(
                request.tokenAmount(),
                plan.getPriceMicros());

        PaymentIntent intent;

        try {
            intent = PaymentIntent.create(
                    PaymentIntentCreateParams.builder()
                            .setAmount(Math.floorDiv(amountLowestUnit, 10_000))
                            .setCurrency(plan.getCurrency().getCode())
                            .putMetadata("account_id", accountId.toString())
                            .build(),
                    RequestOptions.builder()
                            .setIdempotencyKey(request.idempotencyKey())
                            .build()

            );
        } catch (StripeException e) {
            throw new RuntimeException("Stripe PaymentIntent creation failed", e);
        }

        Payment payment = Payment.builder()
                .accountId(accountId)
                .amountLowestUnit(amountLowestUnit)
                .currency(plan.getCurrency())
                .provider("STRIPE")
                .providerPaymentId(intent.getId())
                .idempotentKeyId(request.idempotencyKey())
                .status(PaymentStatusEnum.PENDING)
                .build();

        paymentRepository.saveAndFlush(payment);

        PaymentResponse response =
                new PaymentResponse(intent.getClientSecret());

        storeIdempotentResponse(request.idempotencyKey(), response);

        return response;
    }

    private void storeIdempotentResponse(
            String idempotencyKey,
            PaymentResponse response) {

        try {
            idempotentRepository.save(
                    IdempotentOperation.builder()
                            .idempotencyKey(idempotencyKey)
                            .operation(OperationTypeEnum.PAYMENT.getValue())
                            .requestHash("na")
                            .responseStatus(HttpStatus.OK.value())
                            .responseBody(objectMapper.writeValueAsString(response))
                            .expiresAt(OffsetDateTime.now().plusHours(24))
                            .build()
            );
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

    private void handlePaymentIntent(Event event, PaymentStatusEnum status) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        if (deserializer.getObject().isEmpty()) {
            throw new IllegalStateException("Stripe event data object is empty");
        }

        StripeObject stripeObject = deserializer.getObject().get();

        if (!(stripeObject instanceof PaymentIntent intent)) {
            return;
        }

        Payment payment = paymentRepository
                .findByProviderAndProviderPaymentId("STRIPE", intent.getId())
                .orElseThrow();

        if (payment.getStatus() == status) {
            return;
        }

        payment.setStatus(status);
        paymentRepository.save(payment);

        if (status == PaymentStatusEnum.SUCCEEDED) {
            creditTokens(payment);
        }
    }

    public void handleStripeWebhook(String payload, String signature) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Stripe webhook signature", e);
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> handlePaymentIntent(event, PaymentStatusEnum.SUCCEEDED);
            case "payment_intent.payment_failed" -> handlePaymentIntent(event, PaymentStatusEnum.FAILED);
            default -> {
                return;
            }
        }
    }

    private void handleSucceeded(Event event) {
        PaymentIntent intent = extractIntent(event);

        Payment payment = paymentRepository
                .findByProviderAndProviderPaymentId("STRIPE", intent.getId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Payment not found for Stripe intent " + intent.getId()
                        )
                );

        if (payment.getStatus() == PaymentStatusEnum.SUCCEEDED) {
            return;
        }

        payment.setStatus(PaymentStatusEnum.SUCCEEDED);
        paymentRepository.save(payment);

        creditTokens(payment);
    }

    private PaymentIntent extractIntent(Event event) {
        return (PaymentIntent) event
                .getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() ->
                        new IllegalStateException("Unable to deserialize Stripe event object")
                );
    }

    private void handleFailed(Event event) {
        PaymentIntent intent = extractIntent(event);

        paymentRepository
                .findByIdempotentKeyId(intent.getId())
                .ifPresent(payment -> {
                    payment.setStatus(PaymentStatusEnum.FAILED);
                    paymentRepository.save(payment);
                });
    }

    private void creditTokens(Payment payment) {
        Account account = accountRepository
                .findById(payment.getAccountId())
                .orElseThrow();

        long tokens = calculateTokens(payment);

        account.setTokenBalance(account.getTokenBalance() + tokens);
        accountRepository.save(account);
    }

    private long calculateTokens(Payment payment) {

        Account account = accountRepository
                .findById(payment.getAccountId())
                .orElseThrow();

        PricingPlan plan = account.getPricingPlan();

        return payment.getAmountLowestUnit() / plan.getPriceMicros();
    }

}