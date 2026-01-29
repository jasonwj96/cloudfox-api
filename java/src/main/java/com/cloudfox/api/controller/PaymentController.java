package com.cloudfox.api.controller;

import com.cloudfox.api.dto.request.PaymentRequest;
import com.cloudfox.api.dto.response.PaymentResponse;
import com.cloudfox.api.service.PaymentService;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import jdk.jfr.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cloudfox-api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/intent")
    public PaymentResponse createIntent(
            @AuthenticationPrincipal UUID accountId,
            @RequestBody PaymentRequest request) {

        return paymentService.createPaymentIntent(accountId, request);
    }

    @PostMapping("/stripe/webhook")
    public ResponseEntity<Void> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ) {
        paymentService.handleStripeWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}