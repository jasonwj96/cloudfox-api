package com.cloudfox.api.controller;

import com.cloudfox.api.dto.request.PaymentRequest;
import com.cloudfox.api.dto.response.PaymentResponse;
import com.cloudfox.api.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cloudfox-api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/intent")
    public PaymentResponse createIntent(
            @RequestBody PaymentRequest request,
            @CookieValue("SESSION") UUID sessionToken) {
        return paymentService.createPaymentIntent(sessionToken, request);
    }
}
