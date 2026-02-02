package com.cloudfox.api.dto.response;

import jakarta.validation.constraints.NotBlank;

public record PaymentResponse(

        @NotBlank
        String stripeClientSecret
) {
}