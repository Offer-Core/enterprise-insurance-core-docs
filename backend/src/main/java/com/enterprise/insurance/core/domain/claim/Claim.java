package com.enterprise.insurance.core.domain.claim;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.enterprise.insurance.core.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
                @Index(name = "idx_claims_customer", columnList = "customer_id"),
                @Index(name = "idx_claims_status", columnList = "status"),
                @Index(name = "idx_claims_type", columnList = "claim_type"),
                @Index(name = "idx_claims_incident", columnList = "incident_date"),
                @Index(name = "idx_claims_handler", columnList = "handler_id"),
                @Index(name = "idx_claims_tenant", columnList = "tenant_id")})
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

    @Column(name = "policy_id", nullable = false)
    private UUID policyId;

    @Column(name = "policy_number", length = 30, nullable = false)
    private String policyNumber;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "line_of_business", length = 30, nullable = false)
    private String lineOfBusiness;

    @Column(name = "product_code", length = 50, nullable = false)
    private String productCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "claim_type", length = 50, nullable = false)
    private ClaimType claimType;

    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @Column(name = "incident_time")
    private LocalTime incidentTime;

    @Column(name = "incident_location", columnDefinition = "TEXT")
    private String incidentLocation;

    @Column(name = "incident_latitude", precision = 10, scale = 7)
    private BigDecimal incidentLatitude;

    @Column(name = "incident_longitude", precision = 10, scale = 7)
    private BigDecimal incidentLongitude;

    @Column(name = "reported_date")
    @Builder.Default
    private LocalDateTime reportedDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    @Builder.Default
    private ClaimStatus status = ClaimStatus.REGISTERED;

    @Column(name = "claimed_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal claimedAmount;

    @Column(name = "approved_amount", precision = 15, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "paid_amount", precision = 15, scale = 2)
    private BigDecimal paidAmount;

    @Column(length = 3)
    @Builder.Default
    private String currency = "SAR";

    @Column(name = "excess_amount", precision = 15, scale = 2)
    private BigDecimal excessAmount;

    @Column(name = "excess_type", length = 30)
    private String excessType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "line_specific_data", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> lineSpecificData = Map.of();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dynamic_attributes", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> dynamicAttributes = Map.of();

    @Column(name = "handler_id")
    private UUID handlerId;

    @Column(name = "handler_name", length = 200)
    private String handlerName;

    @Column(name = "adjuster_id")
    private UUID adjusterId;

    @Column(name = "adjuster_name", length = 200)
    private String adjusterName;

    @Column(name = "fraud_score", precision = 3, scale = 2)
    private BigDecimal fraudScore;

    @Column(name = "fraud_review_required")
    @Builder.Default
    private Boolean fraudReviewRequired = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "fraud_indicators", columnDefinition = "jsonb")
    @Builder.Default
    private java.util.List<String> fraudIndicators = java.util.Collections.emptyList();

    @Column(name = "notification_sent")
    @Builder.Default
    private Boolean notificationSent = false;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "settlement_days")
    private Integer settlementDays;

    // ========== Business Logic Methods ==========

    public void transitionTo(ClaimStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "Cannot transition from " + status + " to " + newStatus);
        }
        this.status = newStatus;
    }

    public boolean isClosed() {
        return status == ClaimStatus.CLOSED || status == ClaimStatus.REJECTED
                || status == ClaimStatus.FRAUD_CONFIRMED;
    }

    public boolean isOpen() {
        return !isClosed();
    }

    public boolean isFraudSuspected() {
        return fraudScore != null && fraudScore.compareTo(new BigDecimal("0.50")) >= 0;
    }

    public boolean isHighFraudRisk() {
        return fraudScore != null && fraudScore.compareTo(new BigDecimal("0.75")) >= 0;
    }

    public BigDecimal calculateOutstandingAmount() {
        if (approvedAmount == null) {
            return claimedAmount;
        }
        BigDecimal paid = paidAmount != null ? paidAmount : BigDecimal.ZERO;
        return approvedAmount.subtract(paid);
    }

    public void approve(BigDecimal amount) {
        this.approvedAmount = amount;
        this.status = ClaimStatus.APPROVED;
    }

    public void partialApprove(BigDecimal amount) {
        this.approvedAmount = amount;
        this.status = ClaimStatus.PARTIAL_APPROVED;
    }

    public void reject() {
        this.approvedAmount = BigDecimal.ZERO;
        this.status = ClaimStatus.REJECTED;
    }

    public void pay(BigDecimal amount, String transactionId) {
        this.paidAmount = amount;
        this.status = ClaimStatus.PAID;
    }

    public void close() {
        this.closedAt = LocalDateTime.now();
        if (this.reportedDate != null) {
            this.settlementDays =
                    (int) java.time.Duration.between(this.reportedDate, this.closedAt).toDays();
        }
        this.status = ClaimStatus.CLOSED;
    }

    public void reopen() {
        this.closedAt = null;
        this.settlementDays = null;
        this.status = ClaimStatus.REOPENED;
    }

    public void flagForFraudReview(java.util.List<String> indicators) {
        this.fraudIndicators = indicators;
        this.fraudReviewRequired = true;
        this.status = ClaimStatus.FRAUD_REVIEW;
    }

    public void confirmFraud() {
        this.fraudReviewRequired = true;
        this.status = ClaimStatus.FRAUD_CONFIRMED;
    }

    public void clearFraud() {
        this.fraudIndicators = java.util.Collections.emptyList();
        this.fraudReviewRequired = false;
        this.fraudScore = BigDecimal.ZERO;
        this.status = ClaimStatus.ADJUDICATING;
    }
}
