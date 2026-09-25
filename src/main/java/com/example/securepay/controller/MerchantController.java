package com.example.securepay.controller;

import com.example.securepay.dto.CreateMerchantRequest;
import com.example.securepay.dto.MerchantResponse;
import com.example.securepay.dto.MerchantWebhookResponse;
import com.example.securepay.dto.UpdateWebhookUrlRequest;
import com.example.securepay.entity.Merchant;
import com.example.securepay.service.MerchantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/merchants")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @PostMapping("/register")
    public ResponseEntity<MerchantResponse> registerMerchant(
            @Valid @RequestBody CreateMerchantRequest request
    ) {
        MerchantResponse response =
                merchantService.registerMerchant(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @PutMapping("/webhook")
    public ResponseEntity<MerchantWebhookResponse> updateWebhookUrl(
            @AuthenticationPrincipal Merchant merchant,
            @Valid @RequestBody UpdateWebhookUrlRequest request
    ) {
        MerchantWebhookResponse response =
                merchantService.updateWebhookUrl(
                        merchant.getId(),
                        request
                );

        return ResponseEntity.ok(response);
    }
}