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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.ApiResource;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.stripe.model.PaymentIntent.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final IdempotentOperationRepository idempotentRepository;
    private final ObjectMapper objectMapper;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Transactional
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

        long amountInCents =
                Math.multiplyExact(request.tokenAmount(), plan.getPriceMicros());

        PaymentIntent intent;

        try {
            intent = create(
                    PaymentIntentCreateParams.builder()
                            .setAmount(amountInCents)
                            .setCurrency(plan.getCurrency().getCode())
                            .putMetadata("account_id", accountId.toString())
                            .build(),
                    RequestOptions.builder()
                            .setIdempotencyKey(request.idempotencyKey())
                            .build());
        } catch (StripeException e) {
            throw new RuntimeException("Stripe PaymentIntent creation failed", e);
        }

        Payment payment = Payment.builder()
                .accountId(accountId)
                .provider("STRIPE")
                .providerPaymentId(intent.getId())
                .currency(plan.getCurrency())
                .amountLowestUnit(amountInCents)
                .idempotentKeyId(request.idempotencyKey())
                .tokenAmount(request.tokenAmount())
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

    public void handleStripeWebhook(String payload, String signature) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (Exception e) {
            log.error(e.getMessage());
            return;
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> handleSucceeded(event);
            case "payment_intent.payment_failed" -> handleFailed(event);
        }
    }

    private void handleSucceeded(Event event) {
        PaymentIntent intent = extractIntent(event);



        Payment payment = paymentRepository
                .findByProviderAndProviderPaymentId("STRIPE", intent.getId())
                .orElse(null);

        if (payment == null || payment.getStatus() == PaymentStatusEnum.SUCCEEDED) {
            return;
        }

        payment.setStatus(PaymentStatusEnum.SUCCEEDED);
        paymentRepository.save(payment);

        creditTokens(payment);
    }

    private void handleFailed(Event event) {
        PaymentIntent intent = extractIntent(event);

        paymentRepository
                .findByProviderAndProviderPaymentId("STRIPE", intent.getId())
                .ifPresent(payment -> {
                    payment.setStatus(PaymentStatusEnum.FAILED);
                    paymentRepository.save(payment);
                });
    }

    private PaymentIntent extractIntent(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        if (deserializer.getObject().isPresent()) {
            return (PaymentIntent) deserializer.getObject().get();
        }

        try {
            JsonNode root = objectMapper.readTree(deserializer.getRawJson());

            JsonNode target =
                    root.has("object") && root.get("object").isObject()
                            ? root.get("object")
                            : root;
            return GSON.fromJson(target.toString(), PaymentIntent.class);

        } catch (Exception e) {
            return null;
        }
    }


    private void creditTokens(Payment payment) {
        Account account = accountRepository
                .findById(payment.getAccountId())
                .orElse(null);

        if (account == null) {
            return;
        }

        account.setTokenBalance(
                account.getTokenBalance() + payment.getTokenAmount());

        accountRepository.save(account);
    }
}