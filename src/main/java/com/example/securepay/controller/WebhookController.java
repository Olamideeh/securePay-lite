package com.example.securepay.controller;

import com.example.securepay.dto.WebhookDeliveryResponse;
import com.example.securepay.entity.Merchant;
import com.example.securepay.service.WebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @GetMapping("/{paymentReference}/webhooks")
    public ResponseEntity<List<WebhookDeliveryResponse>>
    getPaymentWebhooks(
            @AuthenticationPrincipal Merchant merchant,
            @PathVariable String paymentReference
    ) {
        List<WebhookDeliveryResponse> response =
                webhookService.getPaymentWebhooks(
                        merchant.getId(),
                        paymentReference
                );

        return ResponseEntity.ok(response);
    }
    @PostMapping(
            "/{paymentReference}/webhooks/{webhookId}/retry"
    )
    public ResponseEntity<WebhookDeliveryResponse>
    retryFailedWebhook(
            @AuthenticationPrincipal Merchant merchant,
            @PathVariable String paymentReference,
            @PathVariable Long webhookId
    ) {
        WebhookDeliveryResponse response =
                webhookService.retryFailedWebhook(
                        merchant.getId(),
                        paymentReference,
                        webhookId
                );

        return ResponseEntity.ok(response);
    }
}