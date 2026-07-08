package com.enterprise.insurance.core.domain;

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
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "policies", schema = "core",
        indexes = {@Index(name = "idx_policies_customer", columnList = "customer_id"),
                @Index(name = "idx_policies_tenant", columnList = "tenant_id"),
                @Index(name = "idx_policies_status", columnList = "status"),
                @Index(name = "idx_policies_product", columnList = "product_code"),
                @Index(name = "idx_policies_dates", columnList = "effective_date, expiry_date"),
                @Index(name = "idx_policies_active", columnList = "status", unique = false)})
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Policy extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "policy_number", length = 30, unique = true, nullable = false)
    private String policyNumber;

    @Column(name = "product_code", length = 50, nullable = false)
    private String productCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "line_of_business", length = 30, nullable = false)
    private LineOfBusiness lineOfBusiness;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "agent_id")
    private UUID agentId;

    @Column(name = "broker_id")
    private UUID brokerId;

    // Policy lifecycle
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyStatus status;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "renewal_date")
    private LocalDate renewalDate;

    // Financials
    @Column(name = "annual_premium", precision = 15, scale = 2, nullable = false)
    private BigDecimal annualPremium;

    @Column(length = 3)
    @Builder.Default
    private String currency = "SAR";

    @Column(name = "tax_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "commission_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal commissionAmount = BigDecimal.ZERO;

    @Column(name = "commission_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal commissionRate = BigDecimal.ZERO;

    @Column(name = "total_premium", precision = 15, scale = 2)
    private BigDecimal totalPremium;

    @Column(name = "payment_frequency", length = 20)
    @Builder.Default
    private String paymentFrequency = "ANNUAL";

    // Dynamic extensions
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "line_specific_data", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> lineSpecificData = Map.of();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dynamic_attributes", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> dynamicAttributes = Map.of();

    // Lifecycle tracking
    @Column(name = "quoted_at")
    private LocalDateTime quotedAt;

    @Column(name = "bound_at")
    private LocalDateTime boundAt;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "cancellation_code", length = 50)
    private String cancellationCode;

    @Column(name = "lapsed_at")
    private LocalDateTime lapsedAt;

    @Column(name = "renewed_at")
    private LocalDateTime renewedAt;

    @Column(name = "previous_policy_id")
    private UUID previousPolicyId;

    @Column(name = "previous_policy_number", length = 30)
    private String previousPolicyNumber;

    @Column(name = "renewal_count")
    @Builder.Default
    private Integer renewalCount = 0;

    // Suspension
    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

    @Column(name = "suspension_reason", length = 500)
    private String suspensionReason;

    // Document
    @Column(name = "policy_document_url", length = 500)
    private String policyDocumentUrl;

    // SAMA compliance
    @Column(name = "sama_reference", length = 50)
    private String samaReference;

    @Column(name = "sama_reported")
    @Builder.Default
    private Boolean samaReported = false;

    @Column(name = "sama_reported_at")
    private LocalDateTime samaReportedAt;

    // Underwriting
    @Column(name = "underwriting_status", length = 20)
    @Builder.Default
    private String underwritingStatus = "PENDING";

    @Column(name = "underwriting_notes", length = 1000)
    private String underwritingNotes;

    @Column(name = "fraud_score", precision = 3, scale = 2)
    private BigDecimal fraudScore;

    @Column(name = "fraud_review_required")
    @Builder.Default
    private Boolean fraudReviewRequired = false;

    // Business logic methods
    public void transitionTo(PolicyStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "Cannot transition from " + status + " to " + newStatus);
        }
        this.status = newStatus;
        LocalDateTime now = LocalDateTime.now();
        switch (newStatus) {
            case QUOTE -> this.quotedAt = now;
            case BOUND -> this.boundAt = now;
            case ISSUED -> this.issuedAt = now;
            case ACTIVE -> this.activatedAt = now;
            case CANCELLED -> this.cancelledAt = now;
            case LAPSED -> this.lapsedAt = now;
            case RENEWED -> {
                this.renewedAt = now;
                this.renewalCount = (this.renewalCount == null ? 0 : this.renewalCount) + 1;
            }
            default -> {
            }
        }
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean isRenewable() {
        return status == PolicyStatus.ACTIVE || status == PolicyStatus.ISSUED;
    }

    public BigDecimal calculateTotalPremium() {
        if (totalPremium == null) {
            totalPremium = annualPremium.add(taxAmount != null ? taxAmount : BigDecimal.ZERO);
        }
        return totalPremium;
    }
}
