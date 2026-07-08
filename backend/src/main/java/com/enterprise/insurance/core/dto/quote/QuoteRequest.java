package com.enterprise.insurance.core.dto.quote;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteRequest {

    @NotBlank
    private String productCode;

    @NotNull
    private UUID customerId;

    @NotNull
    private LocalDate effectiveDate;

    private List<BenefitSelection> selectedBenefits;

    private VehicleDetails vehicleDetails;

    private List<DriverDetails> drivers;

    private String referenceId;

    private Map<String, Object> additionalAttributes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BenefitSelection {
        private String benefitCode;
        private boolean selected;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleDetails {
        private String sequenceNumber;
        private String plateNumber;
        private String plateLetterAr;
        private String plateLetterEn;
        private String chassisNumber;
        private String vehicleMake;
        private String vehicleModel;
        private Integer vehicleYear;
        private String vehicleBodyType;
        private String color;
        private Double estimatedValue;
        private String vehicleUse;
        private String parkingLocation;
        private Integer annualMileage;
        private Boolean antiTheftDevice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverDetails {
        private String nationalId;
        private String fullNameAr;
        private String fullNameEn;
        private LocalDate dateOfBirth;
        private String licenseNumber;
        private String licenseType;
        private LocalDate licenseIssueDate;
        private LocalDate licenseExpiryDate;
        private Integer yearsOfExperience;
        private Boolean isPrimaryDriver;
        private Integer violationsCount;
        private Integer claimsCount;
        private Double noClaimsDiscount;
    }
}
