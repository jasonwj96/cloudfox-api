package com.cloudfox.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.CookieValue;

public record PaymentRequest(

        @Positive
        long amountLowestUnit,

        @NotBlank
        String currency,

        String idempotencyKey
) {
}