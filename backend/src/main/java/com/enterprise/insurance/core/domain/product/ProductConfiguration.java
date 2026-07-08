package com.enterprise.insurance.core.domain.product;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Core product configuration entity. Defines insurance products with JSONB config for
 * extensibility. Supports bilingual (Arabic/English) naming and Saudi market requirements.
 */
@Entity
@Table(name = "product_configurations", schema = "metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "productCode")
public class ProductConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_code", nullable = false, unique = true, length = 50)
    private String productCode;

    @Column(name = "product_name_ar", nullable = false, length = 200)
    private String productNameAr;

    @Column(name = "product_name_en", nullable = false, length = 200)
    private String productNameEn;

    @Column(name = "product_type", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private ProductType productType;

    @Column(name = "line_of_business", nullable = false, length = 30)
    private String lineOfBusiness;

    @Column(name = "product_description_ar", columnDefinition = "TEXT")
    private String productDescriptionAr;

    @Column(name = "product_description_en", columnDefinition = "TEXT")
    private String productDescriptionEn;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "minimum_premium", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal minimumPremium = BigDecimal.ZERO;

    @Column(name = "maximum_premium", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal maximumPremium = new BigDecimal("1000000");

    @Column(name = "config", columnDefinition = "JSONB", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private ProductConfig config = new ProductConfig();

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Version
    @Builder.Default
    private Integer version = 1;

    @Column(name = "tenant_id", length = 50)
    private String tenantId;

    @Column(name = "created_at")
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public boolean isEffective() {
        LocalDate today = LocalDate.now();
        return isActive && !today.isBefore(effectiveFrom)
                && (effectiveTo == null || !today.isAfter(effectiveTo));
    }
}
