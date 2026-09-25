package com.example.securepay.dto;

import java.time.LocalDateTime;

public record AdminMerchantResponse(
        Long id,
        String businessName,
        String email,
        String webhookUrl,
        boolean active,
        LocalDateTime createdAt
) {
}