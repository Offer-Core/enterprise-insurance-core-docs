package com.enterprise.insurance.core.domain.claim;

import java.time.LocalDateTime;
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

@Entity
@Table(name = "fraud_rules", schema = "metadata")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rule_code", length = 50, unique = true, nullable = false)
    private String ruleCode;

    @Column(name = "rule_name_ar", length = 200, nullable = false)
    private String ruleNameAr;

    @Column(name = "rule_name_en", length = 200, nullable = false)
    private String ruleNameEn;

    @Column(name = "rule_type", length = 30, nullable = false)
    private String ruleType;

    @Column(length = 30, nullable = false)
    private String severity;

    @Column
    @Builder.Default
    private Integer weight = 10;

    @Column(length = 500, nullable = false)
    private String condition;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
