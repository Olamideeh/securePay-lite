package com.example.securepay.service;

import com.example.securepay.entity.WebhookDelivery;
import com.example.securepay.enums.WebhookDeliveryStatus;
import com.example.securepay.repository.WebhookDeliveryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class WebhookDeliveryScheduler {

    private final WebhookDeliveryRepository webhookRepository;
    private final WebhookDeliveryService deliveryService;

    public WebhookDeliveryScheduler(
            WebhookDeliveryRepository webhookRepository,
            WebhookDeliveryService deliveryService
    ) {
        this.webhookRepository = webhookRepository;
        this.deliveryService = deliveryService;
    }

    @Scheduled(fixedDelay = 5000)
    public void deliverPendingWebhooks() {
        List<WebhookDelivery> pendingWebhooks =
                webhookRepository
                        .findAllByStatusAndNextRetryAtLessThanEqual(
                                WebhookDeliveryStatus.PENDING,
                                LocalDateTime.now()
                        );

        pendingWebhooks.forEach(deliveryService::deliver);
    }
}