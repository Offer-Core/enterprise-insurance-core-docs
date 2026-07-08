package com.enterprise.insurance.core.domain.claim;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vehicle_damage", schema = "motor",
        indexes = {@Index(name = "idx_vehicle_damage_claim", columnList = "claim_id")})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDamage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "claim_id", nullable = false)
    private UUID claimId;

    @Column(name = "vehicle_component", length = 50)
    private String vehicleComponent;

    @Column(name = "damage_type", length = 50)
    private String damageType;

    @Column(name = "damage_severity", length = 30)
    private String damageSeverity;

    @Column(name = "repair_cost", precision = 15, scale = 2)
    private BigDecimal repairCost;

    @Column(name = "replacement_cost", precision = 15, scale = 2)
    private BigDecimal replacementCost;

    @Column(name = "is_replacement_required")
    @Builder.Default
    private Boolean isReplacementRequired = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<String> photos = List.of();
}
