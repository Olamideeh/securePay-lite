package com.example.securepay.repository;

import com.example.securepay.entity.Payment;
import com.example.securepay.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<Payment> findAllByStatus(
            PaymentStatus status,
            Pageable pageable
    );

    List<Payment> findAllByMerchant_Id(Long merchantId);
    Page<Payment> findAllByMerchant_Id(
            Long merchantId,
            Pageable pageable
    );

    Page<Payment> findAllByMerchant_IdAndStatus(
            Long merchantId,
            PaymentStatus status,
            Pageable pageable
    );
}