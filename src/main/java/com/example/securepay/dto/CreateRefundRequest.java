package com.example.securepay.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateRefundRequest(

        @NotNull(message = "Refund amount is required")
        @DecimalMin(
                value = "0.01",
                message = "Refund amount must be greater than zero"
        )
        BigDecimal amount,

        @Size(
                max = 500,
                message = "Refund reason cannot exceed 500 characters"
        )
        String reason
) {
}