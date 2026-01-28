package com.cloudfox.api.service;

import com.cloudfox.api.dto.request.PaymentRequest;
import com.cloudfox.api.dto.response.PaymentResponse;
import com.cloudfox.api.enums.OperationTypeEnum;
import com.cloudfox.api.enums.PaymentStatusEnum;
import com.cloudfox.api.exceptions.IdempotencyReplayException;
import com.cloudfox.api.model.IdempotentOperation;
import com.cloudfox.api.model.Payment;
import com.cloudfox.api.repository.IdempotentOperationRepository;
import com.cloudfox.api.repository.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {


    private final PaymentRepository paymentRepository;
    private final IdempotentOperationRepository idempotentRepository;
    private final ObjectMapper objectMapper;

    public PaymentResponse createPaymentIntent(
            UUID accountId,
            PaymentRequest request) {


        Optional<IdempotentOperation> operation =
                idempotentRepository.findByIdempotencyKeyAndOperation(
                        request.idempotencyKey(),
                        OperationTypeEnum.PAYMENT.getValue()
                );

        if (operation.isPresent()) {
            return deserialize(operation.get().getResponseBody());
        }
//
//        Payment payment = Payment.builder()
//                .accountId(accountId)
//                .amountLowestUnit(request.tokenAmount() * 10)
//                .currency("USD")
//                .status(PaymentStatusEnum.PENDING)
//                .stripePaymentIntentId("xxxxxxxxx")
//                .build();
//
//        Payment newPayment = paymentRepository.save(payment);
//
//        PaymentResponse response =
//                new PaymentResponse(newPayment.getPublicId());
//
//        storeIdempotentResponse(request.idempotencyKey(), response);
//
//        PaymentIntent intent;
//
//        try {
//            intent = PaymentIntent.create(
//                    PaymentIntentCreateParams.builder()
//                            .setAmount(amountLowestUnit)
//                            .setCurrency(currency)
//                            .putMetadata("account_id", String.valueOf(accountId))
//                            .build()
//            );
//        } catch (StripeException e) {
//            throw new RuntimeException("Stripe PaymentIntent creation failed", e);
//        }

     //   return response;

        return null;
    }

    private void storeIdempotentResponse(
            String idempotencyKey,
            PaymentResponse response
    ) {
        try {
            IdempotentOperation idp = IdempotentOperation.builder()
                    .idempotencyKey(idempotencyKey)
                    .operation(OperationTypeEnum.PAYMENT.getValue())
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
                                    idempotencyKey, OperationTypeEnum.PAYMENT.getValue())
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
