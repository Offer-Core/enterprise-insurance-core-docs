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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "benefit_coverages", schema = "metadata")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BenefitCoverage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "coverage_code", length = 50, nullable = false)
    private String coverageCode;

    @Column(name = "benefit_code", length = 50, nullable = false)
    private String benefitCode;

    @Column(name = "coverage_name_ar", length = 200, nullable = false)
    private String coverageNameAr;

    @Column(name = "coverage_name_en", length = 200)
    private String coverageNameEn;

    @Column(name = "coverage_description_ar", length = 1000)
    private String coverageDescriptionAr;

    @Column(name = "coverage_description_en", length = 1000)
    private String coverageDescriptionEn;

    @Column(name = "coverage_limit", precision = 15, scale = 2)
    private BigDecimal coverageLimit;

    @Column(name = "deductible_amount", precision = 15, scale = 2)
    private BigDecimal deductibleAmount;

    @Column(name = "waiting_period_days")
    private Integer waitingPeriodDays;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

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
