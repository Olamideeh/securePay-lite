package com.example.securepay.service;

import com.example.securepay.entity.WebhookDelivery;
import com.example.securepay.enums.WebhookDeliveryStatus;
import com.example.securepay.repository.WebhookDeliveryRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;

@Service
public class WebhookDeliveryService {

    private final WebhookDeliveryRepository webhookRepository;
    private final RestClient restClient;

    public WebhookDeliveryService(
            WebhookDeliveryRepository webhookRepository,
            RestClient.Builder restClientBuilder
    ) {
        this.webhookRepository = webhookRepository;
        this.restClient = restClientBuilder.build();
    }

    @Transactional
    public void deliver(WebhookDelivery delivery) {
        delivery.setAttemptCount(delivery.getAttemptCount() + 1);
        delivery.setLastAttemptAt(LocalDateTime.now());

        try {
            var response = restClient.post()
                    .uri(delivery.getTargetUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(
                            "X-SecurePay-Signature",
                            delivery.getSignature()
                    )
                    .header(
                            "X-SecurePay-Event",
                            delivery.getEventType().name()
                    )
                    .body(delivery.getPayload())
                    .retrieve()
                    .toBodilessEntity();

            delivery.setResponseStatusCode(
                    response.getStatusCode().value()
            );
            delivery.setStatus(WebhookDeliveryStatus.DELIVERED);
            delivery.setDeliveredAt(LocalDateTime.now());
            delivery.setLastError(null);
            delivery.setNextRetryAt(null);

        } catch (RestClientResponseException exception) {
            delivery.setResponseStatusCode(
                    exception.getStatusCode().value()
            );
            handleDeliveryFailure(delivery, exception.getMessage());

        } catch (RestClientException exception) {
            delivery.setResponseStatusCode(null);
            handleDeliveryFailure(delivery, exception.getMessage());
        }

        webhookRepository.save(delivery);
    }

    private void handleDeliveryFailure(
            WebhookDelivery delivery,
            String errorMessage
    ) {
        delivery.setLastError(limitErrorMessage(errorMessage));
        delivery.setDeliveredAt(null);

        if (delivery.getAttemptCount() >= delivery.getMaxAttempts()) {
            delivery.setStatus(WebhookDeliveryStatus.FAILED);
            delivery.setNextRetryAt(null);
            return;
        }

        delivery.setStatus(WebhookDeliveryStatus.PENDING);

        long delayInSeconds =
                calculateRetryDelay(delivery.getAttemptCount());

        delivery.setNextRetryAt(
                LocalDateTime.now().plusSeconds(delayInSeconds)
        );
    }

    private long calculateRetryDelay(int attemptCount) {
        return switch (attemptCount) {
            case 1 -> 10;
            case 2 -> 30;
            default -> 60;
        };
    }

    private String limitErrorMessage(String errorMessage) {
        if (errorMessage == null) {
            return "Unknown webhook delivery error";
        }

        return errorMessage.length() <= 1000
                ? errorMessage
                : errorMessage.substring(0, 1000);
    }
}