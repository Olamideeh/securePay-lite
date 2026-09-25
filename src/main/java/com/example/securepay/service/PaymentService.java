package com.example.securepay.service;

import com.example.securepay.dto.CreatePaymentRequest;
import com.example.securepay.dto.PaymentResponse;
import com.example.securepay.entity.Customer;
import com.example.securepay.entity.Payment;
import com.example.securepay.enums.PaymentStatus;
import com.example.securepay.exception.IdempotencyConflictException;
import com.example.securepay.exception.ResourceNotFoundException;
import com.example.securepay.repository.CustomerRepository;
import com.example.securepay.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            CustomerRepository customerRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public PaymentResponse createPayment(
            Long merchantId,
            String idempotencyKey,
            CreatePaymentRequest request
    ) {
        String normalizedKey = validateIdempotencyKey(idempotencyKey);

        Payment existingPayment = paymentRepository
                .findByMerchant_IdAndIdempotencyKey(
                        merchantId,
                        normalizedKey
                )
                .orElse(null);

        if (existingPayment != null) {
            validateRepeatedRequest(existingPayment, request);
            return mapToResponse(existingPayment);
        }

        Customer customer = customerRepository
                .findByIdAndMerchant_Id(
                        request.customerId(),
                        merchantId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with ID: "
                                + request.customerId()
                ));

        Payment payment = Payment.builder()
                .reference(generatePaymentReference())
                .idempotencyKey(normalizedKey)
                .amount(request.amount())
                .currency(request.currency())
                .status(PaymentStatus.PENDING)
                .description(normalizeDescription(request.description()))
                .merchant(customer.getMerchant())
                .customer(customer)
                .build();

        return mapToResponse(paymentRepository.save(payment));
    }

    private String validateIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency-Key header is required"
            );
        }

        String normalizedKey = idempotencyKey.trim();

        if (normalizedKey.length() > 100) {
            throw new IllegalArgumentException(
                    "Idempotency key cannot exceed 100 characters"
            );
        }

        return normalizedKey;
    }

    private void validateRepeatedRequest(
            Payment existingPayment,
            CreatePaymentRequest request
    ) {
        boolean sameCustomer =
                existingPayment.getCustomer().getId()
                        .equals(request.customerId());

        boolean sameAmount =
                existingPayment.getAmount()
                        .compareTo(request.amount()) == 0;

        boolean sameCurrency =
                existingPayment.getCurrency() == request.currency();

        boolean sameDescription =
                Objects.equals(
                        existingPayment.getDescription(),
                        normalizeDescription(request.description())
                );

        if (!sameCustomer
                || !sameAmount
                || !sameCurrency
                || !sameDescription) {

            throw new IdempotencyConflictException(
                    "This idempotency key was already used "
                            + "with different payment details"
            );
        }
    }

    private String generatePaymentReference() {
        String reference;

        do {
            reference = "PAY-"
                    + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .toUpperCase();
        } while (paymentRepository.existsByReference(reference));

        return reference;
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getReference(),
                payment.getAmount(),
                payment.getRefundedAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getDescription(),
                payment.getCustomer().getId(),
                payment.getCustomer().getFullName(),
                payment.getProcessedAt(),
                payment.getCreatedAt()
        );
    }
}