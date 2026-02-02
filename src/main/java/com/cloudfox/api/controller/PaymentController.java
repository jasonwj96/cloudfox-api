package com.cloudfox.api.controller;

import com.cloudfox.api.dto.request.PaymentRequest;
import com.cloudfox.api.dto.response.PaymentResponse;
import com.cloudfox.api.service.PaymentService;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import jdk.jfr.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cloudfox-api/v1/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/intent")
    public PaymentResponse createIntent(
            @AuthenticationPrincipal UUID accountId,
            @RequestBody PaymentRequest request) {

        return paymentService.createPaymentIntent(accountId, request);
    }

    @PostMapping(
            value = "stripe/webhook",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        try {
            paymentService.handleStripeWebhook(payload, signature);
        } catch (Exception e) {
            log.error("Stripe webhook handling failed", e);
        }

        return ResponseEntity.ok().build();
    }
}