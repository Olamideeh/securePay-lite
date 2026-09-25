package com.example.securepay.dto;

public record MerchantWebhookResponse(
        Long merchantId,
        String webhookUrl,
        String message
) {
}