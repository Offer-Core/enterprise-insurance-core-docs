package com.enterprise.insurance.core.dto.rating;

import java.math.BigDecimal;
import java.util.Map;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PremiumCalculationRequest {

    @NotBlank(message = "Product code is required")
    private String productCode;

    @NotNull(message = "Base premium is required")
    private BigDecimal basePremium;

    private Map<String, Object> ratingFactors;

    private Integer driverAge;
    private Integer vehicleValue;
    private String vehicleUseType;
    private Integer vehicleModelYear;
    private String parkingLocation;
    private Integer annualMileage;
    private Integer claimFreeYears;
    private Integer violationsCount;
    private Integer accidentsCount;
}
