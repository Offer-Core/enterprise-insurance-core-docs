package com.enterprise.insurance.core.domain.quoting;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponse {

    private UUID quoteId;
    private String quoteReference;
    private String policyNumber;
    private String productCode;

    // Premium breakdown
    private BigDecimal basePremium;
    private BigDecimal taxAmount;
    private BigDecimal totalPremium;
    private String currency;

    // Rating factors applied
    private List<RatingFactorApplied> ratingFactors;

    // Benefits
    private List<BenefitInfo> benefits;

    // Deductibles
    private List<DeductibleInfo> deductibles;

    // Status
    private String status;
    private List<String> warnings;
    private List<String> errors;

    // Validity
    private LocalDateTime quotedAt;
    private LocalDateTime expiresAt;

    // Underwriting
    private String underwritingDecision;
    private List<String> underwritingReferrals;

    // Dynamic
    private Map<String, Object> additionalData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingFactorApplied {
        private String factorCode;
        private String factorName;
        private BigDecimal factorValue;
        private String factorType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BenefitInfo {
        private String benefitCode;
        private String benefitNameAr;
        private String benefitNameEn;
        private BigDecimal benefitAmount;
        private BigDecimal benefitPremium;
        private boolean isOptional;
        private boolean isSelected;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeductibleInfo {
        private String deductibleCode;
        private String deductibleNameAr;
        private String deductibleNameEn;
        private BigDecimal deductibleAmount;
        private String deductibleType;
        private boolean isMandatory;
    }
}
