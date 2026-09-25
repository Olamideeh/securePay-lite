package com.example.securepay.service;

import com.example.securepay.dto.CreateMerchantRequest;
import com.example.securepay.dto.MerchantResponse;
import com.example.securepay.entity.Merchant;
import com.example.securepay.exception.DuplicateResourceException;
import com.example.securepay.repository.MerchantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final ApiKeyService apiKeyService;

    public MerchantService(
            MerchantRepository merchantRepository,
            ApiKeyService apiKeyService
    ) {
        this.merchantRepository = merchantRepository;
        this.apiKeyService = apiKeyService;
    }

    @Transactional
    public MerchantResponse registerMerchant(
            CreateMerchantRequest request
    ) {
        String normalizedEmail = request.email()
                .trim()
                .toLowerCase();

        if (merchantRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException(
                    "A merchant with this email already exists"
            );
        }

        String rawApiKey = apiKeyService.generateApiKey();
        String hashedApiKey = apiKeyService.hashApiKey(rawApiKey);

        Merchant merchant = Merchant.builder()
                .businessName(request.businessName().trim())
                .email(normalizedEmail)
                .webhookUrl(normalizeWebhookUrl(request.webhookUrl()))
                .apiKeyHash(hashedApiKey)
                .active(true)
                .build();

        Merchant savedMerchant = merchantRepository.save(merchant);

        return new MerchantResponse(
                savedMerchant.getId(),
                savedMerchant.getBusinessName(),
                savedMerchant.getEmail(),
                savedMerchant.getWebhookUrl(),
                rawApiKey,
                savedMerchant.isActive(),
                savedMerchant.getCreatedAt()
        );
    }

    private String normalizeWebhookUrl(String webhookUrl) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return null;
        }

        return webhookUrl.trim();
    }
}