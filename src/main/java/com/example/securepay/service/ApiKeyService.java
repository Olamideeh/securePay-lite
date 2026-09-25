package com.example.securepay.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class ApiKeyService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public String generateApiKey() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);

        String randomPart = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        return "sk_test_" + randomPart;
    }

    public String hashApiKey(String rawApiKey) {
        if (rawApiKey == null || rawApiKey.isBlank()) {
            throw new IllegalArgumentException("API key cannot be empty");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashedBytes = digest.digest(
                    rawApiKey.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hashedBytes);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 hashing is unavailable",
                    exception
            );
        }
    }
}