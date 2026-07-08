package com.enterprise.insurance.core.domain.financial;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single component in the premium breakdown. Each component is a rating factor or
 * benefit that contributes to the final premium.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceComponent {

    private String componentCode;
    private String componentNameAr;
    private String componentNameEn;
    private ComponentType componentType;
    private BigDecimal amount;
    private BigDecimal rate;
    private String description;

    public enum ComponentType {
        BASE_PREMIUM, LOADING, DISCOUNT, TAX, FEE, BENEFIT_PREMIUM, COMMISSION, SURCHARGE
    }
}
