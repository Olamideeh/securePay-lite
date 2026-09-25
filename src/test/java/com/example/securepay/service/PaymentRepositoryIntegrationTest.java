package com.example.securepay.repository;

import com.example.securepay.entity.Customer;
import com.example.securepay.entity.Merchant;
import com.example.securepay.entity.Payment;
import com.example.securepay.enums.PaymentStatus;
import com.example.securepay.enums.SupportedCurrency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PaymentRepositoryIntegrationTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void savesAndFindsPaymentByMerchantAndReference() {
        String uniqueValue = UUID.randomUUID().toString();

        Merchant merchant = Merchant.builder()
                .businessName("Integration Test Store")
                .email("merchant-" + uniqueValue + "@example.com")
                .apiKeyHash("hash-" + uniqueValue)
                .active(true)
                .build();

        Merchant savedMerchant =
                merchantRepository.save(merchant);

        Customer customer = Customer.builder()
                .fullName("Integration Customer")
                .email("customer-" + uniqueValue + "@example.com")
                .merchant(savedMerchant)
                .build();

        Customer savedCustomer =
                customerRepository.save(customer);

        Payment payment = Payment.builder()
                .reference("PAY-" + uniqueValue)
                .idempotencyKey("idem-" + uniqueValue)
                .amount(new BigDecimal("5000.00"))
                .currency(SupportedCurrency.NGN)
                .status(PaymentStatus.SUCCESSFUL)
                .merchant(savedMerchant)
                .customer(savedCustomer)
                .build();

        paymentRepository.save(payment);
        paymentRepository.flush();

        Optional<Payment> result =
                paymentRepository
                        .findByReferenceAndMerchant_Id(
                                payment.getReference(),
                                savedMerchant.getId()
                        );

        assertTrue(result.isPresent());
        assertEquals(
                PaymentStatus.SUCCESSFUL,
                result.get().getStatus()
        );
        assertEquals(
                0,
                new BigDecimal("5000.00")
                        .compareTo(result.get().getAmount())
        );
    }

    @Test
    void filtersPaymentsByMerchantAndStatus() {
        String uniqueValue = UUID.randomUUID().toString();

        Merchant merchant = Merchant.builder()
                .businessName("Filter Test Store")
                .email("filter-" + uniqueValue + "@example.com")
                .apiKeyHash("filter-hash-" + uniqueValue)
                .active(true)
                .build();

        Merchant savedMerchant =
                merchantRepository.save(merchant);

        Customer customer = Customer.builder()
                .fullName("Filter Customer")
                .email("filter-customer-" + uniqueValue
                        + "@example.com")
                .merchant(savedMerchant)
                .build();

        Customer savedCustomer =
                customerRepository.save(customer);

        Payment payment = Payment.builder()
                .reference("PAY-FILTER-" + uniqueValue)
                .idempotencyKey("filter-idem-" + uniqueValue)
                .amount(new BigDecimal("2500.00"))
                .currency(SupportedCurrency.NGN)
                .status(PaymentStatus.FAILED)
                .merchant(savedMerchant)
                .customer(savedCustomer)
                .build();

        paymentRepository.saveAndFlush(payment);

        Page<Payment> result =
                paymentRepository
                        .findAllByMerchant_IdAndStatus(
                                savedMerchant.getId(),
                                PaymentStatus.FAILED,
                                PageRequest.of(0, 10)
                        );

        assertEquals(
                "PAY-FILTER-" + uniqueValue,
                result.getContent().get(0).getReference()
        );
    }
}