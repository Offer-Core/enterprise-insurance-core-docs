package com.enterprise.insurance.core.dto.claim;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import com.enterprise.insurance.core.domain.claim.ClaimType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimRegistrationRequest {
    @NotBlank(message = "Policy number is required")
    private String policyNumber;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotNull(message = "Incident date is required")
    private LocalDate incidentDate;

    private String incidentTime;

    private String incidentLocation;

    private BigDecimal incidentLatitude;

    private BigDecimal incidentLongitude;

    @NotNull(message = "Claim type is required")
    private ClaimType claimType;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Claimed amount is required")
    @Positive(message = "Claimed amount must be positive")
    private BigDecimal claimedAmount;

    @Builder.Default
    private String currency = "SAR";

    private MotorClaimDetails motorDetails;

    @Builder.Default
    private Map<String, Object> additionalAttributes = Map.of();

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    @NotNull(message = "Created by is required")
    private String createdBy;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MotorClaimDetails {
        private String accidentType;
        private String accidentCause;
        private String lossLocation;
        private String policeReportNumber;
        private LocalDate policeReportDate;
        private Boolean atFault;
        private Integer faultPercentage;
        private String thirdPartyNationalId;
        private String thirdPartyVehicle;
        private String thirdPartyInsuranceCompany;
        private String repairShopId;
        private BigDecimal repairCostEstimate;
        private Boolean towTruckRequired;
        private java.util.List<VehicleDamage> vehicleDamages;
        private Map<String, Object> additionalAttributes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleDamage {
        private String vehicleComponent;
        private String damageType;
        private String damageSeverity;
        private BigDecimal repairCost;
        private BigDecimal replacementCost;
        private Boolean isReplacementRequired;
        private String notes;
    }
}
