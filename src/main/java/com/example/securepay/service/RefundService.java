package com.example.securepay.service;

import com.example.securepay.dto.CreateRefundRequest;
import com.example.securepay.dto.RefundResponse;
import com.example.securepay.entity.Payment;
import com.example.securepay.entity.Refund;
import com.example.securepay.enums.PaymentStatus;
import com.example.securepay.enums.RefundStatus;
import com.example.securepay.enums.WebhookEventType;
import com.example.securepay.exception.InvalidRefundException;
import com.example.securepay.exception.ResourceNotFoundException;
import com.example.securepay.repository.PaymentRepository;
import com.example.securepay.repository.RefundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefundService {

    private final WebhookService webhookService;
    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;

    public RefundService(
            RefundRepository refundRepository,
            PaymentRepository paymentRepository,
            WebhookService webhookService
    ) {
        this.refundRepository = refundRepository;
        this.paymentRepository = paymentRepository;
        this.webhookService = webhookService;
    }

    @Transactional
    public RefundResponse createRefund(
            Long merchantId,
            String paymentReference,
            CreateRefundRequest request
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

        validatePaymentStatus(payment);

        BigDecimal remainingRefundableAmount =
                payment.getAmount()
                        .subtract(payment.getRefundedAmount());

        if (request.amount()
                .compareTo(remainingRefundableAmount) > 0) {

            throw new InvalidRefundException(
                    "Refund amount exceeds the remaining "
                            + "refundable amount of "
                            + remainingRefundableAmount
            );
        }

        LocalDateTime processedAt = LocalDateTime.now();

        Refund refund = Refund.builder()
                .reference(generateRefundReference())
                .amount(request.amount())
                .status(RefundStatus.SUCCESSFUL)
                .reason(normalizeReason(request.reason()))
                .payment(payment)
                .processedAt(processedAt)
                .build();

        BigDecimal newRefundedAmount =
                payment.getRefundedAmount()
                        .add(request.amount());

        payment.setRefundedAmount(newRefundedAmount);

        if (newRefundedAmount.compareTo(payment.getAmount()) == 0) {
            payment.setStatus(PaymentStatus.REFUNDED);
        } else {
            payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
        }
        Payment savedPayment = paymentRepository.save(payment);
        Refund savedRefund = refundRepository.save(refund);

        webhookService.createWebhook(
                savedPayment,
                WebhookEventType.REFUND_SUCCESSFUL
        );

        return mapToResponse(savedRefund, savedPayment);
    }

    private void validatePaymentStatus(Payment payment) {
        boolean refundable =
                payment.getStatus() == PaymentStatus.SUCCESSFUL
                        || payment.getStatus()
                        == PaymentStatus.PARTIALLY_REFUNDED;

        if (!refundable) {
            throw new InvalidRefundException(
                    "Payment cannot be refunded with status: "
                            + payment.getStatus()
            );
        }
    }

    private String generateRefundReference() {
        String reference;

        do {
            reference = "REF-"
                    + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .toUpperCase();
        } while (refundRepository.existsByReference(reference));

        return reference;
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }

        return reason.trim();
    }

    private RefundResponse mapToResponse(
            Refund refund,
            Payment payment
    ) {
        BigDecimal remaining =
                payment.getAmount()
                        .subtract(payment.getRefundedAmount());

        return new RefundResponse(
                refund.getId(),
                refund.getReference(),
                payment.getReference(),
                refund.getAmount(),
                payment.getRefundedAmount(),
                remaining,
                refund.getStatus(),
                payment.getStatus(),
                refund.getReason(),
                refund.getProcessedAt(),
                refund.getCreatedAt()
        );
    }
}