package com.example.securepay.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String API_KEY_SCHEME =
            "merchantApiKey";

    private static final String BASIC_AUTH_SCHEME =
            "adminBasicAuth";

    @Bean
    public OpenAPI securePayOpenApi() {
        SecurityScheme apiKeyScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-API-Key")
                .description(
                        "Merchant API key beginning with sk_test_"
                );

        SecurityScheme basicAuthScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("basic")
                .description("Admin username and password");

        Components components = new Components()
                .addSecuritySchemes(
                        API_KEY_SCHEME,
                        apiKeyScheme
                )
                .addSecuritySchemes(
                        BASIC_AUTH_SCHEME,
                        basicAuthScheme
                );

        return new OpenAPI()
                .info(new Info()
                        .title("SecurePay API")
                        .version("1.0.0")
                        .description(
                                "Merchant payment simulator with "
                                        + "idempotency, refunds, "
                                        + "signed webhooks and retries."
                        )
                )
                .components(components)
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(API_KEY_SCHEME)
                )
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(BASIC_AUTH_SCHEME)
                );
    }
}