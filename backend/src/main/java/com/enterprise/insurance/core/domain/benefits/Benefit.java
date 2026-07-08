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
@Table(name = "benefits", schema = "metadata")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Benefit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "benefit_code", length = 50, unique = true, nullable = false)
    private String benefitCode;

    @Column(name = "benefit_name_ar", length = 200, nullable = false)
    private String benefitNameAr;

    @Column(name = "benefit_name_en", length = 200)
    private String benefitNameEn;

    @Column(name = "benefit_description_ar", length = 1000)
    private String benefitDescriptionAr;

    @Column(name = "benefit_description_en", length = 1000)
    private String benefitDescriptionEn;

    @Column(name = "benefit_type", length = 30, nullable = false)
    private String benefitType;

    @Column(name = "default_amount", precision = 15, scale = 2)
    private BigDecimal defaultAmount;

    @Column(name = "default_premium", precision = 15, scale = 2)
    private BigDecimal defaultPremium;

    @Column(name = "is_optional")
    @Builder.Default
    private Boolean isOptional = true;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "line_of_business", length = 30)
    private String lineOfBusiness;

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
