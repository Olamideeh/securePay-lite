package com.example.securepay.controller;

import com.example.securepay.dto.CreatePaymentRequest;
import com.example.securepay.dto.PaymentResponse;
import com.example.securepay.dto.ProcessPaymentRequest;
import com.example.securepay.entity.Merchant;
import com.example.securepay.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.securepay.dto.PageResponse;
import com.example.securepay.enums.PaymentStatus;

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
    @PostMapping("/{paymentReference}/process")
    public ResponseEntity<PaymentResponse> processPayment(
            @AuthenticationPrincipal Merchant merchant,
            @PathVariable String paymentReference,
            @Valid @RequestBody ProcessPaymentRequest request
    ) {
        PaymentResponse response =
                paymentService.processPayment(
                        merchant.getId(),
                        paymentReference,
                        request
                );

        return ResponseEntity.ok(response);
    }
    @GetMapping("/{paymentReference}")
    public ResponseEntity<PaymentResponse> getPayment(
            @AuthenticationPrincipal Merchant merchant,
            @PathVariable String paymentReference
    ) {
        PaymentResponse response =
                paymentService.getPayment(
                        merchant.getId(),
                        paymentReference
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<PaymentResponse>>
    getPayments(
            @AuthenticationPrincipal Merchant merchant,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(required = false)
            PaymentStatus status
    ) {
        PageResponse<PaymentResponse> response =
                paymentService.getPayments(
                        merchant.getId(),
                        page,
                        size,
                        status
                );

        return ResponseEntity.ok(response);
    }
}