package com.enterprise.insurance.core.domain.quoting;

import java.math.BigDecimal;

/**
 * Core interface for the rating engine. Implementations are registered per line of business via
 * dependency injection.
 */
public interface RatingEngine {

    /**
     * Calculate a premium quote for the given request.
     *
     * @param request the quote request
     * @return the calculated quote response with full premium breakdown
     */
    QuoteResponse calculatePremium(QuoteRequest request);

    /**
     * The line of business this engine handles.
     */
    String getLineOfBusiness();

    /**
     * Get the base rate for a given product code.
     */
    BigDecimal getBaseRate(String productCode);
}
