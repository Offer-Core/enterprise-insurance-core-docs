package com.enterprise.insurance.core.dto.underwriting;

import java.util.Map;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnderwritingEvaluationRequest {

    @NotBlank(message = "Product code is required")
    private String productCode;

    private Map<String, Object> quoteData;

    private Integer driverAge;
    private Integer vehicleValue;
    private String vehicleUseType;
    private Integer violationsCount;
    private Integer accidentsCount;
    private String nationality;
    private String occupation;
    private Boolean licenseValid;
}
