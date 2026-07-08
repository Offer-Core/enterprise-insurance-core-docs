package com.enterprise.insurance.lines.motor.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MotorQuoteRequest {

    @NotBlank(message = "National ID is required")
    @Size(min = 10, max = 10, message = "National ID must be 10 digits")
    private String nationalId;

    @NotBlank(message = "Full name (Arabic) is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullNameAr;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^05[0-9]{8}$", message = "Mobile number must be a valid Saudi number")
    private String mobile;

    private String email;

    @Valid
    @NotNull(message = "Vehicle details are required")
    private VehicleRequest vehicle;

    @Valid
    private List<DriverRequest> drivers;

    @Data
    public static class VehicleRequest {
        @NotBlank(message = "Plate number is required")
        private String plateNumber;

        @NotBlank(message = "Vehicle make is required")
        private String vehicleMake;

        @NotBlank(message = "Vehicle model is required")
        private String vehicleModel;

        @NotNull(message = "Vehicle year is required")
        @Min(value = 1990, message = "Vehicle year must be >= 1990")
        @Max(value = 2026, message = "Vehicle year must be <= current year")
        private Integer vehicleYear;

        private String chassisNumber;
    }

    @Data
    public static class DriverRequest {
        @NotBlank(message = "National ID is required for each driver")
        private String nationalId;

        @NotBlank(message = "Driving license number is required")
        private String drivingLicenseNumber;

        @NotNull(message = "License expiry date is required")
        @Future(message = "License expiry date must be in the future")
        private LocalDate licenseExpiryDate;

        private Boolean isPrimaryDriver = false;
    }
}
