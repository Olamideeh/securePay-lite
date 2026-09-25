package com.example.securepay.security;

import com.example.securepay.entity.Merchant;
import com.example.securepay.repository.MerchantRepository;
import com.example.securepay.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    private final MerchantRepository merchantRepository;
    private final ApiKeyService apiKeyService;

    public ApiKeyAuthenticationFilter(
            MerchantRepository merchantRepository,
            ApiKeyService apiKeyService
    ) {
        this.merchantRepository = merchantRepository;
        this.apiKeyService = apiKeyService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String rawApiKey = request.getHeader(API_KEY_HEADER);

        if (rawApiKey != null && !rawApiKey.isBlank()) {
            String hashedApiKey = apiKeyService.hashApiKey(rawApiKey);

            merchantRepository.findByApiKeyHash(hashedApiKey)
                    .filter(Merchant::isActive)
                    .ifPresent(this::authenticateMerchant);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateMerchant(Merchant merchant) {
        var authority = new SimpleGrantedAuthority("ROLE_MERCHANT");

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        merchant,
                        null,
                        List.of(authority)
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }
}