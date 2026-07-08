package com.enterprise.insurance.lines.motor.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MotorPolicyResponse {
    private UUID policyId;
    private String policyNumber;
    private String status;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    private BigDecimal premiumAmount;
    private String currency;
    private VehicleResponse vehicle;
    private BigDecimal finalPremium;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleResponse {
        private String plateNumber;
        private String vehicleMake;
        private String vehicleModel;
        private Integer vehicleYear;
    }
}
