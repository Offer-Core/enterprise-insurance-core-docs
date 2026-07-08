package com.enterprise.insurance.core.dto.renewal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenewalResponse {

    private String oldPolicyNumber;
    private String newPolicyNumber;
    private BigDecimal previousPremium;
    private BigDecimal newPremium;
    private BigDecimal premiumChange;
    private BigDecimal premiumChangePercent;
    private LocalDate newEffectiveDate;
    private LocalDate newExpiryDate;
    private String status;
    private List<String> changes;
}
