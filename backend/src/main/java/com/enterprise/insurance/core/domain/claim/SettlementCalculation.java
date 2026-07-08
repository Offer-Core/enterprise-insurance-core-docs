package com.enterprise.insurance.core.domain.claim;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementCalculation {
    private BigDecimal totalApprovedAmount;
    private BigDecimal deductibleAmount;
    private BigDecimal excessAmount;
    private BigDecimal salvageAmount;
    private BigDecimal netPayableAmount;
    private List<SettlementComponent> components;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettlementComponent {
        private String componentType;
        private String description;
        private BigDecimal amount;
    }
}
