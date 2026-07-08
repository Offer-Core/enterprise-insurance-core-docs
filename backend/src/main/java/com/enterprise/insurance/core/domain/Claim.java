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
@Table(name = "claims", schema = "core",
        indexes = {@Index(name = "idx_claims_policy", columnList = "policy_id"),
                @Index(name = "idx_claims_status", columnList = "status"),
                @Index(name = "idx_claims_handler", columnList = "handler_id"),
                @Index(name = "idx_claims_tenant", columnList = "tenant_id"),
                @Index(name = "idx_claims_fraud", columnList = "fraud_review_required",
                        unique = false)})
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Claim extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "claim_number", length = 30, unique = true, nullable = false)
    private String claimNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // Claim lifecycle
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ClaimStatus status = ClaimStatus.REPORTED;

    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "closure_reason", length = 500)
    private String closureReason;

    // Financial
    @Column(name = "claimed_amount", precision = 15, scale = 2)
    private BigDecimal claimedAmount;

    @Column(name = "approved_amount", precision = 15, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "paid_amount", precision = 15, scale = 2)
    private BigDecimal paidAmount;

    @Column(length = 3)
    @Builder.Default
    private String currency = "SAR";

    @Column(name = "excess_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal excessAmount = BigDecimal.ZERO;

    @Column(name = "deductible_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal deductibleAmount = BigDecimal.ZERO;

    @Column(name = "reserve_amount", precision = 15, scale = 2)
    private BigDecimal reserveAmount;

    // Dynamic extensions
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "line_specific_data", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> lineSpecificData = Map.of();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dynamic_attributes", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> dynamicAttributes = Map.of();

    // Assignment
    @Column(name = "handler_id")
    private UUID handlerId;

    @Column(name = "handler_name", length = 200)
    private String handlerName;

    @Column(name = "adjuster_id")
    private UUID adjusterId;

    @Column(name = "adjuster_name", length = 200)
    private String adjusterName;

    // Fraud indicators
    @Column(name = "fraud_score", precision = 3, scale = 2)
    private BigDecimal fraudScore;

    @Column(name = "fraud_review_required")
    @Builder.Default
    private Boolean fraudReviewRequired = false;

    @Column(name = "fraud_review_notes", length = 1000)
    private String fraudReviewNotes;

    // Third party
    @Column(name = "third_party_national_id", length = 20)
    private String thirdPartyNationalId;

    @Column(name = "third_party_name", length = 200)
    private String thirdPartyName;

    @Column(name = "third_party_insurance_company", length = 200)
    private String thirdPartyInsuranceCompany;

    @Column(name = "third_party_policy_number", length = 30)
    private String thirdPartyPolicyNumber;

    // SAMA compliance
    @Column(name = "sama_reference", length = 50)
    private String samaReference;

    @Column(name = "sama_reported")
    @Builder.Default
    private Boolean samaReported = false;

    @Column(name = "sama_reported_at")
    private LocalDateTime samaReportedAt;

    // Business logic methods
    public void transitionTo(ClaimStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "Cannot transition from " + status + " to " + newStatus);
        }
        this.status = newStatus;
        if (newStatus == ClaimStatus.CLOSED) {
            this.closedAt = LocalDateTime.now();
        }
    }

    public BigDecimal getOutstandingAmount() {
        if (approvedAmount == null)
            return claimedAmount;
        if (paidAmount == null)
            return approvedAmount;
        return approvedAmount.subtract(paidAmount);
    }

    public boolean isOverdue() {
        return status == ClaimStatus.ADJUDICATED && approvedAmount != null && paidAmount == null
                && reportedAt != null && reportedAt.plusDays(30).isBefore(LocalDateTime.now());
    }
}
