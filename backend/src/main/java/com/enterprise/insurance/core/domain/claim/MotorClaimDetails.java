package com.enterprise.insurance.core.domain.claim;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "motor_claim_details", schema = "motor",
        indexes = {@Index(name = "idx_motor_claim_details_claim", columnList = "claim_id")})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MotorClaimDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "claim_id", nullable = false)
    private UUID claimId;

    @Column(name = "accident_type", length = 50)
    private String accidentType;

    @Column(name = "accident_cause", length = 100)
    private String accidentCause;

    @Column(name = "accident_description", columnDefinition = "TEXT")
    private String accidentDescription;

    @Column(name = "loss_location", columnDefinition = "TEXT")
    private String lossLocation;

    @Column(name = "loss_latitude", precision = 10, scale = 7)
    private BigDecimal lossLatitude;

    @Column(name = "loss_longitude", precision = 10, scale = 7)
    private BigDecimal lossLongitude;

    @Column(name = "police_report_number", length = 50)
    private String policeReportNumber;

    @Column(name = "police_report_date")
    private LocalDate policeReportDate;

    @Column(name = "police_station_name", length = 200)
    private String policeStationName;

    @Column(name = "police_station_code", length = 50)
    private String policeStationCode;

    @Column(name = "at_fault")
    private Boolean atFault;

    @Column(name = "fault_percentage")
    private Integer faultPercentage;

    @Column(name = "weather_conditions", length = 30)
    private String weatherConditions;

    @Column(name = "road_conditions", length = 30)
    private String roadConditions;

    @Column(name = "traffic_conditions", length = 30)
    private String trafficConditions;

    @Column(name = "time_of_day", length = 30)
    private String timeOfDay;

    // Third party
    @Column(name = "third_party_national_id", columnDefinition = "TEXT")
    private String thirdPartyNationalId;

    @Column(name = "third_party_name", length = 200)
    private String thirdPartyName;

    @Column(name = "third_party_vehicle", length = 200)
    private String thirdPartyVehicle;

    @Column(name = "third_party_plate_number", length = 20)
    private String thirdPartyPlateNumber;

    @Column(name = "third_party_insurance_company", length = 200)
    private String thirdPartyInsuranceCompany;

    @Column(name = "third_party_policy_number", length = 30)
    private String thirdPartyPolicyNumber;

    @Column(name = "third_party_claim_number", length = 30)
    private String thirdPartyClaimNumber;

    @Column(name = "third_party_liability_percentage")
    private Integer thirdPartyLiabilityPercentage;

    // Repair
    @Column(name = "repair_shop_id", length = 50)
    private String repairShopId;

    @Column(name = "repair_shop_name", length = 200)
    private String repairShopName;

    @Column(name = "repair_shop_phone", length = 20)
    private String repairShopPhone;

    @Column(name = "repair_cost_estimate", precision = 15, scale = 2)
    private BigDecimal repairCostEstimate;

    @Column(name = "repair_cost_actual", precision = 15, scale = 2)
    private BigDecimal repairCostActual;

    @Column(name = "repair_authorized_at")
    private LocalDateTime repairAuthorizedAt;

    @Column(name = "repair_completed_at")
    private LocalDateTime repairCompletedAt;

    @Column(name = "repair_status", length = 30)
    @Builder.Default
    private String repairStatus = "PENDING";

    // Towing
    @Column(name = "tow_truck_required")
    @Builder.Default
    private Boolean towTruckRequired = false;

    @Column(name = "tow_truck_company", length = 200)
    private String towTruckCompany;

    @Column(name = "tow_truck_license_plate", length = 20)
    private String towTruckLicensePlate;

    @Column(name = "tow_truck_dispatched_at")
    private LocalDateTime towTruckDispatchedAt;

    @Column(name = "tow_truck_arrived_at")
    private LocalDateTime towTruckArrivedAt;

    @Column(name = "tow_truck_cost", precision = 15, scale = 2)
    private BigDecimal towTruckCost;

    @Column(name = "tow_drop_off_location", columnDefinition = "TEXT")
    private String towDropOffLocation;

    // Appraisal
    @Column(name = "appraisal_reference", length = 50)
    private String appraisalReference;

    @Column(name = "appraiser_name", length = 200)
    private String appraiserName;

    @Column(name = "appraiser_company", length = 200)
    private String appraiserCompany;

    @Column(name = "appraisal_date")
    private LocalDate appraisalDate;

    @Column(name = "salvage_value", precision = 15, scale = 2)
    private BigDecimal salvageValue;

    @Column(name = "salvage_company", length = 200)
    private String salvageCompany;

    @Column(name = "total_loss")
    @Builder.Default
    private Boolean totalLoss = false;

    @Column(name = "damage_assessment", columnDefinition = "TEXT")
    private String damageAssessment;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dynamic_attributes", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> dynamicAttributes = Map.of();
}
