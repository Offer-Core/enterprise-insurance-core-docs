package com.enterprise.insurance.core.domain.rating;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Specific values/ranges for each rating factor. Maps to metadata.rating_factor_values table.
 */
@Entity
@Table(name = "rating_factor_values", schema = "metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingFactorValue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "factor_id", nullable = false)
    private UUID factorId;

    @Column(name = "value_code", nullable = false, length = 50)
    private String valueCode;

    @Column(name = "value_label_ar", length = 200)
    private String valueLabelAr;

    @Column(name = "value_label_en", length = 200)
    private String valueLabelEn;

    @Column(name = "min_value", length = 50)
    private String minValue;

    @Column(name = "max_value", length = 50)
    private String maxValue;

    @Column(name = "factor_value", nullable = false, precision = 10, scale = 4)
    private BigDecimal factorValue;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "tenant_id", length = 50)
    private String tenantId;

    @Column(name = "created_at")
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}
