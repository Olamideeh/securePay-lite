package com.example.securepay.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateWebhookUrlRequest(

        @NotBlank(message = "Webhook URL is required")
        @Size(
                max = 500,
                message = "Webhook URL cannot exceed 500 characters"
        )
        @Pattern(
                regexp = "^https?://.+$",
                message = "Webhook URL must begin with http:// or https://"
        )
        String webhookUrl
) {
}