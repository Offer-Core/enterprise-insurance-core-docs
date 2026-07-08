package com.enterprise.insurance.core.dto.rating;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PremiumCalculationResponse {

    private BigDecimal basePremium;
    private BigDecimal totalPremium;
    private BigDecimal taxAmount;
    private BigDecimal finalPremium;
    private String currency;
    private List<FactorApplication> factorApplications;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FactorApplication {
        private String factorCode;
        private String factorName;
        private String factorType;
        private BigDecimal value;
        private String description;
    }
}
