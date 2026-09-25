package com.example.securepay.dto;

import java.time.LocalDateTime;

public record MerchantResponse(
        Long id,
        String businessName,
        String email,
        String webhookUrl,
        String apiKey,
        boolean active,
        LocalDateTime createdAt
) {
}