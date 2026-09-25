package com.example.securepay.service;

import com.example.securepay.dto.AdminMerchantResponse;
import com.example.securepay.dto.AdminPaymentResponse;
import com.example.securepay.dto.AdminWebhookResponse;
import com.example.securepay.entity.Merchant;
import com.example.securepay.entity.Payment;
import com.example.securepay.entity.WebhookDelivery;
import com.example.securepay.enums.WebhookDeliveryStatus;
import com.example.securepay.repository.MerchantRepository;
import com.example.securepay.repository.PaymentRepository;
import com.example.securepay.repository.WebhookDeliveryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final MerchantRepository merchantRepository;
    private final PaymentRepository paymentRepository;
    private final WebhookDeliveryRepository webhookRepository;

    public AdminService(
            MerchantRepository merchantRepository,
            PaymentRepository paymentRepository,
            WebhookDeliveryRepository webhookRepository
    ) {
        this.merchantRepository = merchantRepository;
        this.paymentRepository = paymentRepository;
        this.webhookRepository = webhookRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminMerchantResponse> getAllMerchants() {
        return merchantRepository.findAll()
                .stream()
                .map(this::mapMerchant)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminPaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::mapPayment)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminWebhookResponse> getFailedWebhooks() {
        return webhookRepository
                .findAllByStatus(WebhookDeliveryStatus.FAILED)
                .stream()
                .map(this::mapWebhook)
                .toList();
    }

    private AdminMerchantResponse mapMerchant(Merchant merchant) {
        return new AdminMerchantResponse(
                merchant.getId(),
                merchant.getBusinessName(),
                merchant.getEmail(),
                merchant.getWebhookUrl(),
                merchant.isActive(),
                merchant.getCreatedAt()
        );
    }

    private AdminPaymentResponse mapPayment(Payment payment) {
        return new AdminPaymentResponse(
                payment.getId(),
                payment.getReference(),
                payment.getAmount(),
                payment.getRefundedAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getMerchant().getId(),
                payment.getMerchant().getBusinessName(),
                payment.getCustomer().getId(),
                payment.getCustomer().getFullName(),
                payment.getProcessedAt(),
                payment.getCreatedAt()
        );
    }

    private AdminWebhookResponse mapWebhook(
            WebhookDelivery delivery
    ) {
        Payment payment = delivery.getPayment();
        Merchant merchant = payment.getMerchant();

        return new AdminWebhookResponse(
                delivery.getId(),
                payment.getReference(),
                merchant.getId(),
                merchant.getBusinessName(),
                delivery.getEventType(),
                delivery.getTargetUrl(),
                delivery.getStatus(),
                delivery.getAttemptCount(),
                delivery.getMaxAttempts(),
                delivery.getResponseStatusCode(),
                delivery.getLastError(),
                delivery.getLastAttemptAt(),
                delivery.getCreatedAt()
        );
    }
}