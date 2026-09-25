package com.example.securepay.service;

import com.example.securepay.dto.WebhookPayload;
import com.example.securepay.entity.Merchant;
import com.example.securepay.entity.Payment;
import com.example.securepay.entity.WebhookDelivery;
import com.example.securepay.enums.WebhookDeliveryStatus;
import com.example.securepay.enums.WebhookEventType;
import com.example.securepay.repository.WebhookDeliveryRepository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class WebhookService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final WebhookDeliveryRepository webhookRepository;
    private final ObjectMapper objectMapper;

    public WebhookService(
            WebhookDeliveryRepository webhookRepository,
            ObjectMapper objectMapper
    ) {
        this.webhookRepository = webhookRepository;
        this.objectMapper = objectMapper;
    }

    public Optional<WebhookDelivery> createWebhook(
            Payment payment,
            WebhookEventType eventType
    ) {
        Merchant merchant = payment.getMerchant();

        if (merchant.getWebhookUrl() == null
                || merchant.getWebhookUrl().isBlank()) {
            return Optional.empty();
        }

        WebhookPayload webhookPayload = new WebhookPayload(
                eventType,
                payment.getReference(),
                payment.getAmount(),
                payment.getRefundedAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                LocalDateTime.now()
        );

        String payload = convertToJson(webhookPayload);
        String signature = generateSignature(
                payload,
                merchant.getApiKeyHash()
        );

        WebhookDelivery delivery = WebhookDelivery.builder()
                .eventType(eventType)
                .targetUrl(merchant.getWebhookUrl())
                .payload(payload)
                .signature(signature)
                .status(WebhookDeliveryStatus.PENDING)
                .attemptCount(0)
                .maxAttempts(3)
                .nextRetryAt(LocalDateTime.now())
                .payment(payment)
                .build();

        return Optional.of(webhookRepository.save(delivery));
    }

    private String convertToJson(WebhookPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Could not create webhook payload",
                    exception
            );
        }
    }

    private String generateSignature(
            String payload,
            String signingKey
    ) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);

            SecretKeySpec key = new SecretKeySpec(
                    signingKey.getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM
            );

            mac.init(key);

            byte[] signatureBytes = mac.doFinal(
                    payload.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(signatureBytes);

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Could not sign webhook payload",
                    exception
            );
        }
    }
}