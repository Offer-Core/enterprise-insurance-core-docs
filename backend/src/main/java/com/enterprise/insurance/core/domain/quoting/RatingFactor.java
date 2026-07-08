package com.enterprise.insurance.core.domain.quoting;

import java.math.BigDecimal;

/**
 * Interface for pluggable rating factors used in the rating engine. Each factor calculates a
 * multiplier or additive value based on the quote request.
 */
public interface RatingFactor {

    /**
     * Unique code identifying this rating factor (e.g., "DRIVER_AGE", "NCD", "VEHICLE_VALUE").
     */
    String getFactorCode();

    /**
     * Display name in Arabic.
     */
    String getFactorNameAr();

    /**
     * Display name in English.
     */
    String getFactorNameEn();

    /**
     * The type of factor: MULTIPLIER, ADDITIVE, or BASE_RATE.
     */
    FactorType getFactorType();

    /**
     * Calculate the factor value for the given quote request.
     *
     * @param request the quote request containing all relevant data
     * @return the calculated factor value
     */
    BigDecimal calculate(QuoteRequest request);

    /**
     * The line of business this factor applies to.
     */
    String getLineOfBusiness();

    enum FactorType {
        MULTIPLIER, ADDITIVE, BASE_RATE, DISCOUNT, SURCHARGE
    }
}
