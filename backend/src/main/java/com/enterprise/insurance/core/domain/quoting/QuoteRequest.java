package com.enterprise.insurance.core.domain.quoting;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.enterprise.insurance.core.domain.LineOfBusiness;
import jakarta.validation.Valid;
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
    private String nationalId;

    private String identityType;

    // Customer info
    private String fullNameAr;
    private String fullNameEn;
    private LocalDate dateOfBirthGregorian;
    private String dateOfBirthHijri;
    private String gender;
    private String mobileNumber;
    private String email;

    // Product
    @NotBlank
    private String productCode;

    @NotNull
    private LineOfBusiness lineOfBusiness;

    // Vehicle details (for motor)
    @Valid
    private VehicleInfo vehicle;

    // Drivers (for motor)
    @Valid
    private List<DriverInfo> drivers;

    // Dynamic attributes
    private Map<String, Object> additionalAttributes;

    // Metadata
    private String channel;
    private String source;
    private UUID agentId;
    private UUID brokerId;
    private String tenantId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleInfo {
        private String sequenceNumber;
        private String plateNumber;
        private String plateType;
        private String plateLetterAr;
        private String plateLetterEn;
        private String chassisNumber;
        private String vehicleMake;
        private String vehicleModel;
        private Integer vehicleYear;
        private String vehicleBodyType;
        private String color;
        private BigDecimal estimatedValue;
        private String vehicleUse;
        private String parkingLocation;
        private Integer annualMileage;
        private Boolean antiTheftDevice;
        private String engineCapacity;
        private Integer numberOfSeats;
        private Integer numberOfDoors;
        private String transmissionType;
        private String fuelType;
        private String registrationExpiry;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverInfo {
        private String nationalId;
        private String fullNameAr;
        private String fullNameEn;
        private LocalDate dateOfBirth;
        private String dateOfBirthHijri;
        private String gender;
        private String licenseNumber;
        private String licenseType;
        private LocalDate licenseIssueDate;
        private LocalDate licenseExpiryDate;
        private Integer yearsOfExperience;
        private Boolean isPrimaryDriver;
        private Integer violationsCount;
        private Integer claimsCount;
        private BigDecimal noClaimsDiscount;
        private List<ViolationInfo> violations;
        private List<AccidentInfo> accidents;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ViolationInfo {
        private String violationCode;
        private String violationDescription;
        private LocalDate violationDate;
        private Integer points;
        private BigDecimal fineAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccidentInfo {
        private String accidentReference;
        private LocalDate accidentDate;
        private String accidentType;
        private Boolean atFault;
        private BigDecimal damageAmount;
    }
}
