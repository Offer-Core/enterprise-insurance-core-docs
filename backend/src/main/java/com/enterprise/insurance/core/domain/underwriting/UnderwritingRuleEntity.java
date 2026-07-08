package com.enterprise.insurance.core.domain.underwriting;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * JPA entity for configurable underwriting rules. Maps to metadata.underwriting_rules table.
 */
@Entity
@Table(name = "underwriting_rules", schema = "metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "ruleCode")
public class UnderwritingRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rule_code", nullable = false, unique = true, length = 50)
    private String ruleCode;

    @Column(name = "rule_name_ar", nullable = false, length = 200)
    private String ruleNameAr;

    @Column(name = "rule_name_en", nullable = false, length = 200)
    private String ruleNameEn;

    @Column(name = "rule_type", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private UnderwritingRuleType ruleType;

    @Column(name = "severity", length = 30)
    @Builder.Default
    private String severity = "WARNING";

    @Column(name = "condition", nullable = false, length = 500)
    private String condition;

    @Column(name = "action", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private UnderwritingAction action;

    @Column(name = "surcharge_amount", precision = 15, scale = 2)
    private BigDecimal surchargeAmount;

    @Column(name = "referral_reason", columnDefinition = "TEXT")
    private String referralReason;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "tenant_id", length = 50)
    private String tenantId;

    @Column(name = "created_at")
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
