package com.example.securepay.dto;

import com.example.securepay.enums.PaymentOutcome;
import jakarta.validation.constraints.NotNull;

public record ProcessPaymentRequest(

        @NotNull(message = "Payment outcome is required")
        PaymentOutcome outcome
) {
}