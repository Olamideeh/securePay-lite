package com.example.securepay.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMerchantRequest(

        @NotBlank(message = "Business name is required")
        @Size(max = 150, message = "Business name cannot exceed 150 characters")
        String businessName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @Size(max = 500, message = "Webhook URL cannot exceed 500 characters")
        String webhookUrl
) {
}