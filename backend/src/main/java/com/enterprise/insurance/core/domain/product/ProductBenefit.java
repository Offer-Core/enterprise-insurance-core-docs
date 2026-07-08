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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Benefits assigned to a specific product. Defines optional and mandatory benefits with pricing.
 */
@Entity
@Table(name = "product_benefits", schema = "metadata",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "benefit_code"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"productId", "benefitCode"})
public class ProductBenefit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "benefit_code", nullable = false, length = 50)
    private String benefitCode;

    @Column(name = "benefit_name_ar", nullable = false, length = 200)
    private String benefitNameAr;

    @Column(name = "benefit_name_en", nullable = false, length = 200)
    private String benefitNameEn;

    @Column(name = "benefit_description_ar", columnDefinition = "TEXT")
    private String benefitDescriptionAr;

    @Column(name = "benefit_description_en", columnDefinition = "TEXT")
    private String benefitDescriptionEn;

    @Column(name = "benefit_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private BenefitType benefitType;

    @Column(name = "default_price", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal defaultPrice = BigDecimal.ZERO;

    @Column(name = "price_override", precision = 15, scale = 2)
    private BigDecimal priceOverride;

    @Column(name = "is_optional")
    @Builder.Default
    private Boolean isOptional = true;

    @Column(name = "is_included_by_default")
    @Builder.Default
    private Boolean isIncludedByDefault = false;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "conditions", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private String conditions = "{}";

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

    public BigDecimal getEffectivePrice() {
        return priceOverride != null ? priceOverride : defaultPrice;
    }
}
