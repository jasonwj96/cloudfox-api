package com.cloudfox.api.dto.response;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record PaymentResponse(

        @NotBlank
        UUID paymentKey
) {
}