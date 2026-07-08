package com.enterprise.insurance.core.domain.benefits;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_benefits", schema = "metadata",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"product_code", "benefit_code"})})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBenefit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_code", length = 50, nullable = false)
    private String productCode;

    @Column(name = "benefit_code", length = 50, nullable = false)
    private String benefitCode;

    @Column(name = "benefit_amount", precision = 15, scale = 2)
    private BigDecimal benefitAmount;

    @Column(name = "benefit_premium", precision = 15, scale = 2)
    private BigDecimal benefitPremium;

    @Column(name = "is_mandatory")
    @Builder.Default
    private Boolean isMandatory = false;

    @Column(name = "is_default_selected")
    @Builder.Default
    private Boolean isDefaultSelected = false;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
