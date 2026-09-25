package com.example.securepay.controller;

import com.example.securepay.dto.CreatePaymentRequest;
import com.example.securepay.dto.PaymentResponse;
import com.example.securepay.entity.Merchant;
import com.example.securepay.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @AuthenticationPrincipal Merchant merchant,

            @RequestHeader(
                    value = "Idempotency-Key",
                    required = false
            )
            String idempotencyKey,

            @Valid @RequestBody CreatePaymentRequest request
    ) {
        PaymentResponse response =
                paymentService.createPayment(
                        merchant.getId(),
                        idempotencyKey,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}