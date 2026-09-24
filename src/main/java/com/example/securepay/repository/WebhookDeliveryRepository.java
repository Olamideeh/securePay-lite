package com.example.securepay.repository;

import com.example.securepay.entity.WebhookDelivery;
import com.example.securepay.enums.WebhookDeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WebhookDeliveryRepository
        extends JpaRepository<WebhookDelivery, Long> {

    List<WebhookDelivery> findAllByPayment_Id(
            Long paymentId
    );

    List<WebhookDelivery>
    findAllByStatusAndNextRetryAtLessThanEqual(
            WebhookDeliveryStatus status,
            LocalDateTime retryTime
    );
}