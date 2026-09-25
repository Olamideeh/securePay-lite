package com.example.securepay.controller;

import com.example.securepay.dto.CreateRefundRequest;
import com.example.securepay.dto.RefundResponse;
import com.example.securepay.entity.Merchant;
import com.example.securepay.service.RefundService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping("/{paymentReference}/refunds")
    public ResponseEntity<RefundResponse> createRefund(
            @AuthenticationPrincipal Merchant merchant,
            @PathVariable String paymentReference,
            @Valid @RequestBody CreateRefundRequest request
    ) {
        RefundResponse response =
                refundService.createRefund(
                        merchant.getId(),
                        paymentReference,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}