package com.example.securepay.repository;

import com.example.securepay.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefundRepository
        extends JpaRepository<Refund, Long> {

    boolean existsByReference(String reference);

    Optional<Refund> findByReference(String reference);

    List<Refund> findAllByPayment_Id(Long paymentId);
}