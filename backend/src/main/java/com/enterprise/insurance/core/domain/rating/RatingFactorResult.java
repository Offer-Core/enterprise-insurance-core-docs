package com.enterprise.insurance.core.domain.rating;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of applying a single rating factor to a premium calculation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingFactorResult {

    private String factorCode;
    private String factorName;
    private RatingFactorType factorType;
    private BigDecimal value;
    private String description;

    /**
     * Apply this factor result to a base premium amount.
     *
     * @param base the base premium amount
     * @return the adjusted premium after applying this factor
     */
    public BigDecimal apply(BigDecimal base) {
        if (base == null || value == null) {
            return base;
        }
        return switch (factorType) {
            case MULTIPLIER -> base.multiply(value);
            case ADDITIVE -> base.add(value);
            case SUBTRACTIVE -> base.subtract(value);
            case FORMULA -> base.multiply(BigDecimal.ONE.add(value));
            case TIERED -> base.multiply(value);
        };
    }
}
