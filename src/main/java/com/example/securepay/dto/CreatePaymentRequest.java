package com.example.securepay.dto;

import com.example.securepay.enums.SupportedCurrency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreatePaymentRequest(

        @NotNull(message = "Customer ID is required")
        Long customerId,

        @NotNull(message = "Amount is required")
        @DecimalMin(
                value = "0.01",
                message = "Amount must be greater than zero"
        )
        BigDecimal amount,

        @NotNull(message = "Currency is required")
        SupportedCurrency currency,

        @Size(
                max = 500,
                message = "Description cannot exceed 500 characters"
        )
        String description
) {
}