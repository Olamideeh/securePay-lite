package com.example.securepay.service;

import com.example.securepay.dto.CreatePaymentRequest;
import com.example.securepay.dto.PaymentResponse;
import com.example.securepay.entity.Customer;
import com.example.securepay.entity.Merchant;
import com.example.securepay.entity.Payment;
import com.example.securepay.enums.PaymentStatus;
import com.example.securepay.enums.SupportedCurrency;
import com.example.securepay.exception.IdempotencyConflictException;
import com.example.securepay.repository.CustomerRepository;
import com.example.securepay.repository.PaymentRepository;
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
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private WebhookService webhookService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createPayment_withNewKey_createsPendingPayment() {
        Merchant merchant = Merchant.builder()
                .id(1L)
                .businessName("Qosim Store")
                .build();

        Customer customer = Customer.builder()
                .id(1L)
                .fullName("Aisha Bello")
                .merchant(merchant)
                .build();

        CreatePaymentRequest request =
                new CreatePaymentRequest(
                        1L,
                        new BigDecimal("5000.00"),
                        SupportedCurrency.NGN,
                        "Java course payment"
                );

        when(paymentRepository
                .findByMerchant_IdAndIdempotencyKey(
                        1L,
                        "order-1001"
                ))
                .thenReturn(Optional.empty());

        when(customerRepository
                .findByIdAndMerchant_Id(1L, 1L))
                .thenReturn(Optional.of(customer));

        when(paymentRepository.existsByReference(anyString()))
                .thenReturn(false);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> {
                    Payment payment = invocation.getArgument(0);
                    payment.setId(1L);
                    return payment;
                });

        PaymentResponse response =
                paymentService.createPayment(
                        1L,
                        "order-1001",
                        request
                );

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(PaymentStatus.PENDING, response.status());
        assertEquals(
                0,
                new BigDecimal("5000.00")
                        .compareTo(response.amount())
        );
        assertTrue(response.reference().startsWith("PAY-"));

        verify(paymentRepository, times(1))
                .save(any(Payment.class));
    }

    @Test
    void createPayment_withSameKeyAndSameDetails_returnsExistingPayment() {
        Payment existingPayment = createExistingPayment();

        when(paymentRepository
                .findByMerchant_IdAndIdempotencyKey(
                        1L,
                        "order-1001"
                ))
                .thenReturn(Optional.of(existingPayment));

        CreatePaymentRequest request =
                new CreatePaymentRequest(
                        1L,
                        new BigDecimal("5000.00"),
                        SupportedCurrency.NGN,
                        "Java course payment"
                );

        PaymentResponse response =
                paymentService.createPayment(
                        1L,
                        "order-1001",
                        request
                );

        assertEquals("PAY-EXISTING", response.reference());

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }

    @Test
    void createPayment_withSameKeyAndDifferentAmount_throwsConflict() {
        Payment existingPayment = createExistingPayment();

        when(paymentRepository
                .findByMerchant_IdAndIdempotencyKey(
                        1L,
                        "order-1001"
                ))
                .thenReturn(Optional.of(existingPayment));

        CreatePaymentRequest request =
                new CreatePaymentRequest(
                        1L,
                        new BigDecimal("7000.00"),
                        SupportedCurrency.NGN,
                        "Java course payment"
                );

        assertThrows(
                IdempotencyConflictException.class,
                () -> paymentService.createPayment(
                        1L,
                        "order-1001",
                        request
                )
        );

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }

    private Payment createExistingPayment() {
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
                .reference("PAY-EXISTING")
                .idempotencyKey("order-1001")
                .amount(new BigDecimal("5000.00"))
                .currency(SupportedCurrency.NGN)
                .status(PaymentStatus.PENDING)
                .description("Java course payment")
                .merchant(merchant)
                .customer(customer)
                .build();
    }
}