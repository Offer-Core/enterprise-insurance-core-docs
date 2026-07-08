package com.enterprise.insurance.core.domain.product;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Global benefit catalog entity. Defines all available benefits that can be assigned to products.
 */
@Entity
@Table(name = "benefit_catalog", schema = "metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "benefitCode")
public class BenefitCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "benefit_code", nullable = false, unique = true, length = 50)
    private String benefitCode;

    @Column(name = "benefit_name_ar", nullable = false, length = 200)
    private String benefitNameAr;

    @Column(name = "benefit_name_en", nullable = false, length = 200)
    private String benefitNameEn;

    @Column(name = "benefit_description_ar", columnDefinition = "TEXT")
    private String benefitDescriptionAr;

    @Column(name = "benefit_description_en", columnDefinition = "TEXT")
    private String benefitDescriptionEn;

    @Column(name = "benefit_category", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private BenefitCategory benefitCategory;

    @Column(name = "default_price", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal defaultPrice = BigDecimal.ZERO;

    @Column(name = "maximum_price", precision = 15, scale = 2)
    private BigDecimal maximumPrice;

    @Column(name = "calculation_method", length = 30)
    @Enumerated(EnumType.STRING)
    private CalculationMethod calculationMethod;

    @Column(name = "calculation_params", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private String calculationParams = "{}";

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "applicable_product_types", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private String applicableProductTypes = "[]";

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

    public enum BenefitCategory {
        STANDARD, PREMIUM, CUSTOM
    }

    public enum CalculationMethod {
        FIXED, PERCENTAGE, FORMULA
    }
}
