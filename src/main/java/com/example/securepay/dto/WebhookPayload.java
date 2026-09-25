package com.example.securepay.dto;

import com.example.securepay.enums.PaymentStatus;
import com.example.securepay.enums.SupportedCurrency;
import com.example.securepay.enums.WebhookEventType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WebhookPayload(
        WebhookEventType event,
        String paymentReference,
        BigDecimal paymentAmount,
        BigDecimal refundedAmount,
        SupportedCurrency currency,
        PaymentStatus paymentStatus,
        LocalDateTime occurredAt
) {
}