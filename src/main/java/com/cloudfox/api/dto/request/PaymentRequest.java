package com.cloudfox.api.dto.request;

public record PaymentRequest(
        String idempotencyKey,

        int tokenAmount
) {
}