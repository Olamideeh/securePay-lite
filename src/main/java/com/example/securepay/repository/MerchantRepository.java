package com.example.securepay.repository;

import com.example.securepay.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantRepository
        extends JpaRepository<Merchant, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<Merchant> findByEmailIgnoreCase(String email);

    Optional<Merchant> findByApiKeyHash(String apiKeyHash);
}