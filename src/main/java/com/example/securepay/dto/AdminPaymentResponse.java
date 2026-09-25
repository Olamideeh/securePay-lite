package com.example.securepay.dto;

import com.example.securepay.enums.PaymentStatus;
import com.example.securepay.enums.SupportedCurrency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminPaymentResponse(
        Long id,
        String reference,
        BigDecimal amount,
        BigDecimal refundedAmount,
        SupportedCurrency currency,
        PaymentStatus status,
        Long merchantId,
        String merchantName,
        Long customerId,
        String customerName,
        LocalDateTime processedAt,
        LocalDateTime createdAt
) {
}