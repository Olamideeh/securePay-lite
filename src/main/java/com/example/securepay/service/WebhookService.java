package com.example.securepay.service;

import com.example.securepay.dto.WebhookDeliveryResponse;
import com.example.securepay.dto.WebhookPayload;
import com.example.securepay.entity.Merchant;
import com.example.securepay.entity.Payment;
import com.example.securepay.entity.WebhookDelivery;
import com.example.securepay.enums.WebhookDeliveryStatus;
import com.example.securepay.enums.WebhookEventType;
import com.example.securepay.exception.ResourceNotFoundException;
import com.example.securepay.repository.PaymentRepository;
import com.example.securepay.repository.WebhookDeliveryRepository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
public class WebhookService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final PaymentRepository paymentRepository;
    private final WebhookDeliveryRepository webhookRepository;
    private final ObjectMapper objectMapper;

    public WebhookService(
            WebhookDeliveryRepository webhookRepository,
            PaymentRepository paymentRepository,
            ObjectMapper objectMapper
    ) {
        this.webhookRepository = webhookRepository;
        this.paymentRepository = paymentRepository;
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

    @Transactional(readOnly = true)
    public List<WebhookDeliveryResponse> getPaymentWebhooks(
            Long merchantId,
            String paymentReference
    ) {
        Payment payment = paymentRepository
                .findByReferenceAndMerchant_Id(
                        paymentReference,
                        merchantId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with reference: "
                                + paymentReference
                ));

        return webhookRepository
                .findAllByPayment_Id(payment.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private WebhookDeliveryResponse mapToResponse(
            WebhookDelivery delivery
    ) {
        return new WebhookDeliveryResponse(
                delivery.getId(),
                delivery.getEventType(),
                delivery.getTargetUrl(),
                delivery.getStatus(),
                delivery.getAttemptCount(),
                delivery.getMaxAttempts(),
                delivery.getResponseStatusCode(),
                delivery.getLastError(),
                delivery.getLastAttemptAt(),
                delivery.getNextRetryAt(),
                delivery.getDeliveredAt(),
                delivery.getCreatedAt()
        );
    }
    @Transactional
    public WebhookDeliveryResponse retryFailedWebhook(
            Long merchantId,
            String paymentReference,
            Long webhookId
    ) {
        Payment payment = paymentRepository
                .findByReferenceAndMerchant_Id(
                        paymentReference,
                        merchantId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with reference: "
                                + paymentReference
                ));

        WebhookDelivery delivery = webhookRepository
                .findById(webhookId)
                .filter(webhook ->
                        webhook.getPayment()
                                .getId()
                                .equals(payment.getId())
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Webhook delivery not found with ID: "
                                + webhookId
                ));

        if (delivery.getStatus()
                != WebhookDeliveryStatus.FAILED) {
            throw new IllegalArgumentException(
                    "Only a FAILED webhook can be retried manually"
            );
        }

        delivery.setTargetUrl(
                payment.getMerchant().getWebhookUrl()
        );
        delivery.setStatus(WebhookDeliveryStatus.PENDING);
        delivery.setAttemptCount(0);
        delivery.setResponseStatusCode(null);
        delivery.setLastError(null);
        delivery.setLastAttemptAt(null);
        delivery.setNextRetryAt(LocalDateTime.now());
        delivery.setDeliveredAt(null);

        return mapToResponse(webhookRepository.save(delivery));
    }
}