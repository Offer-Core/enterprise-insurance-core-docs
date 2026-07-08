package com.enterprise.insurance.core.dto.quote;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponse {

    private String quoteNumber;
    private String productCode;
    private String productNameAr;
    private String productNameEn;
    private BigDecimal basePremium;
    private BigDecimal totalPremium;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal surchargeAmount;
    private String currency;
    private List<RatingFactorResult> factors;
    private List<BenefitDetail> benefits;
    private List<UnderwritingResult> underwritingResults;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    private LocalDateTime expiresAt;
    private String status;
    private List<String> warnings;
    private List<String> errors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingFactorResult {
        private String factorCode;
        private String factorName;
        private BigDecimal factorValue;
        private String factorType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BenefitDetail {
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
    public static class UnderwritingResult {
        private String ruleCode;
        private String ruleName;
        private String decision;
        private String reason;
    }

    public Map<String, Object> toMap() {
        return Map.of("quoteNumber", quoteNumber, "productCode", productCode, "basePremium",
                basePremium, "totalPremium", totalPremium, "taxAmount", taxAmount, "status", status,
                "expiresAt", expiresAt != null ? expiresAt.toString() : null);
    }
}
