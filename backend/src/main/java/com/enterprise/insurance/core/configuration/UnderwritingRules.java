package com.enterprise.insurance.core.configuration;

import java.util.List;
import com.enterprise.insurance.core.domain.quoting.QuoteRequest;
import com.enterprise.insurance.core.domain.quoting.QuoteResponse;

/**
 * Interface for underwriting rules engine. Rules are configuration-driven and can be modified
 * without code changes.
 */
public interface UnderwritingRules {

    /**
     * Evaluate underwriting rules for a quote request.
     *
     * @param request the quote request
     * @return underwriting decision result
     */
    UnderwritingResult evaluate(QuoteRequest request);

    /**
     * Apply post-calculation underwriting rules to the quote response.
     *
     * @param request the original quote request
     * @param response the calculated quote response
     * @return updated quote response with underwriting decisions
     */
    QuoteResponse applyRules(QuoteRequest request, QuoteResponse response);

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class UnderwritingResult {
        private String decision; // ACCEPT, REFER, DECLINE
        private List<String> referrals;
        private List<String> reasons;
        private List<String> warnings;
        private boolean requiresManualReview;
    }
}
