package com.enterprise.insurance.core.domain.rating;

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
 * JPA entity for configurable rating factors stored in the database. Maps to
 * metadata.rating_factors table.
 */
@Entity
@Table(name = "rating_factors", schema = "metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "factorCode")
public class RatingFactorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "factor_code", nullable = false, unique = true, length = 50)
    private String factorCode;

    @Column(name = "factor_name_ar", nullable = false, length = 200)
    private String factorNameAr;

    @Column(name = "factor_name_en", nullable = false, length = 200)
    private String factorNameEn;

    @Column(name = "factor_type", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private RatingFactorType factorType;

    @Column(name = "calculation_method", length = 30)
    private String calculationMethod;

    @Column(name = "parameters", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private String parameters = "{}";

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
