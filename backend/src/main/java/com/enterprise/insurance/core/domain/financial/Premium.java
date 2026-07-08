package com.enterprise.insurance.core.domain.financial;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Value object representing a premium calculation result with full breakdown.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Premium {

    private BigDecimal basePremium;
    private BigDecimal loadingAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal feeAmount;
    private BigDecimal commissionAmount;
    private BigDecimal totalPremium;
    private String currency;

    @Builder.Default
    private BigDecimal taxRate = BigDecimal.valueOf(0.15); // 15% VAT in KSA

    private List<PriceComponent> components;

    public BigDecimal getNetPremium() {
        return basePremium.add(loadingAmount).subtract(discountAmount);
    }

    public BigDecimal calculateTotal() {
        BigDecimal net = getNetPremium();
        BigDecimal tax = net.multiply(taxRate);
        return net.add(tax).add(feeAmount);
    }
}
