package com.example.securepay.dto;

import com.example.securepay.enums.WebhookDeliveryStatus;
import com.example.securepay.enums.WebhookEventType;

import java.time.LocalDateTime;

public record AdminWebhookResponse(
        Long id,
        String paymentReference,
        Long merchantId,
        String merchantName,
        WebhookEventType eventType,
        String targetUrl,
        WebhookDeliveryStatus status,
        int attemptCount,
        int maxAttempts,
        Integer responseStatusCode,
        String lastError,
        LocalDateTime lastAttemptAt,
        LocalDateTime createdAt
) {
}