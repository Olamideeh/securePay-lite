package com.example.securepay.dto;

import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String fullName,
        String email,
        String phoneNumber,
        Long merchantId,
        LocalDateTime createdAt
) {
}