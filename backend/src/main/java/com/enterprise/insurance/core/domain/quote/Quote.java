package com.enterprise.insurance.core.domain.quote;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quotes", schema = "core",
        indexes = {@Index(name = "idx_quotes_customer", columnList = "customer_id"),
                @Index(name = "idx_quotes_status", columnList = "status"),
                @Index(name = "idx_quotes_expires", columnList = "expires_at"),
                @Index(name = "idx_quotes_product", columnList = "product_code"),
                @Index(name = "idx_quotes_tenant", columnList = "tenant_id")})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "quote_number", length = 30, unique = true, nullable = false)
    private String quoteNumber;

    @Column(name = "policy_id")
    private UUID policyId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "product_code", length = 50, nullable = false)
    private String productCode;

    @Column(name = "product_name_ar", length = 255)
    private String productNameAr;

    @Column(name = "product_name_en", length = 255)
    private String productNameEn;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "base_premium", precision = 15, scale = 2, nullable = false)
    private BigDecimal basePremium;

    @Column(name = "final_premium", precision = 15, scale = 2, nullable = false)
    private BigDecimal finalPremium;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "surcharge_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal surchargeAmount = BigDecimal.ZERO;

    @Column(length = 3)
    @Builder.Default
    private String currency = "SAR";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rating_factors", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> ratingFactors = Map.of();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "benefits", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> benefits = Map.of();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "vehicle_details", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> vehicleDetails = Map.of();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "driver_details", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> driverDetails = Map.of();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "underwriting_results", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> underwritingResults = Map.of();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private QuoteStatus status = QuoteStatus.DRAFT;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejected_reason", length = 500)
    private String rejectedReason;

    @Column(name = "reference_id", length = 50)
    private String referenceId;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusHours(24);
        }
    }

    public void transitionTo(QuoteStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "Cannot transition from " + status + " to " + newStatus);
        }
        this.status = newStatus;
        LocalDateTime now = LocalDateTime.now();
        switch (newStatus) {
            case ACCEPTED -> this.acceptedAt = now;
            case REJECTED -> this.rejectedAt = now;
            default -> {
            }
        }
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
}
