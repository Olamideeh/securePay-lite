package com.example.securepay.service;

import com.example.securepay.dto.CreateRefundRequest;
import com.example.securepay.dto.RefundResponse;
import com.example.securepay.entity.Customer;
import com.example.securepay.entity.Merchant;
import com.example.securepay.entity.Payment;
import com.example.securepay.entity.Refund;
import com.example.securepay.enums.PaymentStatus;
import com.example.securepay.enums.SupportedCurrency;
import com.example.securepay.exception.InvalidRefundException;
import com.example.securepay.repository.PaymentRepository;
import com.example.securepay.repository.RefundRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private WebhookService webhookService;

    @InjectMocks
    private RefundService refundService;

    @Test
    void createRefund_withValidPartialAmount_updatesPayment() {
        Payment payment = createPayment(
                PaymentStatus.SUCCESSFUL,
                "5000.00",
                "0.00"
        );

        when(paymentRepository
                .findByReferenceAndMerchant_Id(
                        "PAY-001",
                        1L
                ))
                .thenReturn(Optional.of(payment));

        when(refundRepository.existsByReference(anyString()))
                .thenReturn(false);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        when(refundRepository.save(any(Refund.class)))
                .thenAnswer(invocation -> {
                    Refund refund = invocation.getArgument(0);
                    refund.setId(1L);
                    return refund;
                });

        CreateRefundRequest request =
                new CreateRefundRequest(
                        new BigDecimal("2000.00"),
                        "Partial return"
                );

        RefundResponse response =
                refundService.createRefund(
                        1L,
                        "PAY-001",
                        request
                );

        assertEquals(
                PaymentStatus.PARTIALLY_REFUNDED,
                response.paymentStatus()
        );

        assertEquals(
                0,
                new BigDecimal("2000.00")
                        .compareTo(response.totalRefundedAmount())
        );

        assertEquals(
                0,
                new BigDecimal("3000.00")
                        .compareTo(
                                response.remainingRefundableAmount()
                        )
        );

        verify(webhookService, times(1))
                .createWebhook(
                        any(Payment.class),
                        any()
                );
    }

    @Test
    void createRefund_exceedingRemainingAmount_throwsException() {
        Payment payment = createPayment(
                PaymentStatus.PARTIALLY_REFUNDED,
                "5000.00",
                "3000.00"
        );

        when(paymentRepository
                .findByReferenceAndMerchant_Id(
                        "PAY-001",
                        1L
                ))
                .thenReturn(Optional.of(payment));

        CreateRefundRequest request =
                new CreateRefundRequest(
                        new BigDecimal("2500.00"),
                        "Excessive refund"
                );

        assertThrows(
                InvalidRefundException.class,
                () -> refundService.createRefund(
                        1L,
                        "PAY-001",
                        request
                )
        );

        verify(refundRepository, never())
                .save(any(Refund.class));
    }

    @Test
    void createRefund_forFailedPayment_throwsException() {
        Payment payment = createPayment(
                PaymentStatus.FAILED,
                "5000.00",
                "0.00"
        );

        when(paymentRepository
                .findByReferenceAndMerchant_Id(
                        "PAY-001",
                        1L
                ))
                .thenReturn(Optional.of(payment));

        CreateRefundRequest request =
                new CreateRefundRequest(
                        new BigDecimal("1000.00"),
                        "Invalid refund"
                );

        assertThrows(
                InvalidRefundException.class,
                () -> refundService.createRefund(
                        1L,
                        "PAY-001",
                        request
                )
        );

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verify(refundRepository, never())
                .save(any(Refund.class));
    }

    private Payment createPayment(
            PaymentStatus status,
            String amount,
            String refundedAmount
    ) {
        Merchant merchant = Merchant.builder()
                .id(1L)
                .businessName("Qosim Store")
                .build();

        Customer customer = Customer.builder()
                .id(1L)
                .fullName("Aisha Bello")
                .merchant(merchant)
                .build();

        return Payment.builder()
                .id(1L)
                .reference("PAY-001")
                .idempotencyKey("order-1001")
                .amount(new BigDecimal(amount))
                .refundedAmount(new BigDecimal(refundedAmount))
                .currency(SupportedCurrency.NGN)
                .status(status)
                .merchant(merchant)
                .customer(customer)
                .build();
    }
}