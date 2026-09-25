package com.example.securepay.dto;

import com.example.securepay.enums.PaymentStatus;
import com.example.securepay.enums.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RefundResponse(
        Long id,
        String reference,
        String paymentReference,
        BigDecimal refundAmount,
        BigDecimal totalRefundedAmount,
        BigDecimal remainingRefundableAmount,
        RefundStatus refundStatus,
        PaymentStatus paymentStatus,
        String reason,
        LocalDateTime processedAt,
        LocalDateTime createdAt
) {
}