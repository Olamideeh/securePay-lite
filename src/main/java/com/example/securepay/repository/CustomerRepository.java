package com.example.securepay.repository;

import com.example.securepay.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    boolean existsByMerchant_IdAndEmailIgnoreCase(
            Long merchantId,
            String email
    );

    Optional<Customer> findByIdAndMerchant_Id(
            Long customerId,
            Long merchantId
    );

    List<Customer> findAllByMerchant_Id(Long merchantId);
}