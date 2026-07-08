package com.enterprise.insurance.core.domain.endorsement;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "endorsements", schema = "core",
        indexes = {@Index(name = "idx_endorsements_policy", columnList = "policy_id"),
                @Index(name = "idx_endorsements_status", columnList = "status"),
                @Index(name = "idx_endorsements_tenant", columnList = "tenant_id")})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Endorsement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "endorsement_number", length = 30, unique = true, nullable = false)
    private String endorsementNumber;

    @Column(name = "policy_id", nullable = false)
    private UUID policyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "endorsement_type", length = 50, nullable = false)
    private EndorsementType endorsementType;

    @Column(length = 500)
    private String description;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "prior_state", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> priorState;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_state", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> newState;

    @Column(name = "premium_adjustment", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal premiumAdjustment = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EndorsementStatus status = EndorsementStatus.REQUESTED;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_by")
    private UUID rejectedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejected_reason", length = 500)
    private String rejectedReason;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    public void approve(UUID approvedBy) {
        if (this.status != EndorsementStatus.REQUESTED
                && this.status != EndorsementStatus.REVIEWING) {
            throw new IllegalStateException("Cannot approve endorsement in status: " + this.status);
        }
        this.status = EndorsementStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject(UUID rejectedBy, String reason) {
        if (this.status != EndorsementStatus.REQUESTED
                && this.status != EndorsementStatus.REVIEWING) {
            throw new IllegalStateException("Cannot reject endorsement in status: " + this.status);
        }
        this.status = EndorsementStatus.REJECTED;
        this.rejectedBy = rejectedBy;
        this.rejectedAt = LocalDateTime.now();
        this.rejectedReason = reason;
    }

    public void apply() {
        if (this.status != EndorsementStatus.APPROVED) {
            throw new IllegalStateException("Cannot apply endorsement in status: " + this.status);
        }
        this.status = EndorsementStatus.EFFECTIVE;
        this.appliedAt = LocalDateTime.now();
    }
}
