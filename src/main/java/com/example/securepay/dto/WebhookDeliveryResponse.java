package com.example.securepay.dto;

import com.example.securepay.enums.WebhookDeliveryStatus;
import com.example.securepay.enums.WebhookEventType;

import java.time.LocalDateTime;

public record WebhookDeliveryResponse(
        Long id,
        WebhookEventType eventType,
        String targetUrl,
        WebhookDeliveryStatus status,
        int attemptCount,
        int maxAttempts,
        Integer responseStatusCode,
        String lastError,
        LocalDateTime lastAttemptAt,
        LocalDateTime nextRetryAt,
        LocalDateTime deliveredAt,
        LocalDateTime createdAt
) {
}