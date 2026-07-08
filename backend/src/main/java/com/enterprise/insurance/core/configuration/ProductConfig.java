package com.enterprise.insurance.core.configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Product configuration entity stored in the metadata schema. Supports event-sourced metadata for
 * versioning and audit.
 */
@Entity
@Table(name = "product_configurations", schema = "metadata", indexes = {
        @Index(name = "idx_product_config_code", columnList = "product_code", unique = true),
        @Index(name = "idx_product_config_lob", columnList = "line_of_business"),
        @Index(name = "idx_product_config_active", columnList = "is_active", unique = false)})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_code", length = 50, unique = true, nullable = false)
    private String productCode;

    @Column(name = "product_name_ar", length = 200, nullable = false)
    private String productNameAr;

    @Column(name = "product_name_en", length = 200)
    private String productNameEn;

    @Column(name = "product_description_ar", length = 1000)
    private String productDescriptionAr;

    @Column(name = "product_description_en", length = 1000)
    private String productDescriptionEn;

    @Column(name = "line_of_business", length = 30, nullable = false)
    private String lineOfBusiness;

    @Column(name = "product_type", length = 30)
    private String productType;

    @Column(name = "base_rate", precision = 15, scale = 2)
    private BigDecimal baseRate;

    @Column(name = "tax_rate", precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.valueOf(0.15);

    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @Column(name = "min_premium", precision = 15, scale = 2)
    private BigDecimal minPremium;

    @Column(name = "max_premium", precision = 15, scale = 2)
    private BigDecimal maxPremium;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "SAR";

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_configurable")
    @Builder.Default
    private Boolean isConfigurable = true;

    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @Column(name = "version")
    @Builder.Default
    private Integer version = 1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "configuration", columnDefinition = "jsonb")
    private Map<String, Object> configuration;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "underwriting_rules", columnDefinition = "jsonb")
    private Map<String, Object> underwritingRules;

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
