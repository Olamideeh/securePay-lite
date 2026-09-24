package com.example.securepay.repository;

import com.example.securepay.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    boolean existsByReference(String reference);

    Optional<Payment> findByReferenceAndMerchant_Id(
            String reference,
            Long merchantId
    );

    Optional<Payment> findByMerchant_IdAndIdempotencyKey(
            Long merchantId,
            String idempotencyKey
    );

    List<Payment> findAllByMerchant_Id(Long merchantId);
}