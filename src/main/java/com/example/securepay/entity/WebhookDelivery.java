package com.example.securepay.entity;

import com.example.securepay.enums.WebhookDeliveryStatus;
import com.example.securepay.enums.WebhookEventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WebhookEventType eventType;

    @Column(nullable = false)
    private String targetUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    private String signature;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WebhookDeliveryStatus status =
            WebhookDeliveryStatus.PENDING;

    @Builder.Default
    @Column(nullable = false)
    private int attemptCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private int maxAttempts = 3;

    private Integer responseStatusCode;

    @Column(length = 1000)
    private String lastError;

    private LocalDateTime lastAttemptAt;

    private LocalDateTime nextRetryAt;

    private LocalDateTime deliveredAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "payment_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_webhook_payment")
    )
    private Payment payment;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void beforeInsert() {
        LocalDateTime now = LocalDateTime.now();

        if (status == null) {
            status = WebhookDeliveryStatus.PENDING;
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void beforeUpdate() {
        updatedAt = LocalDateTime.now();
    }
}