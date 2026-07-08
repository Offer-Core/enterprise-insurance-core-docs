package com.enterprise.insurance.core.domain.product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JSONB configuration for product extensibility. Contains underwriting, rating, billing, claims,
 * coverages, and exclusions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductConfig {

    @Builder.Default
    private UnderwritingConfig underwriting = new UnderwritingConfig();

    @Builder.Default
    private RatingConfig rating = new RatingConfig();

    @Builder.Default
    private BillingConfig billing = new BillingConfig();

    @Builder.Default
    private ClaimsConfig claims = new ClaimsConfig();

    @Builder.Default
    private List<CoverageConfig> coverages = new ArrayList<>();

    @Builder.Default
    private List<ExclusionConfig> exclusions = new ArrayList<>();

    @Builder.Default
    private Map<String, Object> extendedAttributes = new HashMap<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UnderwritingConfig {
        private Integer minimumAge;
        private Integer maximumAge;
        private Boolean requiresMedicalExam;
        @Builder.Default
        private List<String> excludedNationalities = new ArrayList<>();
        @Builder.Default
        private List<String> excludedOccupations = new ArrayList<>();
        @Builder.Default
        private List<String> referralRules = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RatingConfig {
        private String baseRateFormula;
        @Builder.Default
        private List<RatingFactorConfig> factors = new ArrayList<>();
        @Builder.Default
        private BigDecimal taxRate = new java.math.BigDecimal("0.15");
        @Builder.Default
        private String currency = "SAR";
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BillingConfig {
        @Builder.Default
        private List<String> allowedPaymentMethods = new ArrayList<>();
        @Builder.Default
        private List<String> allowedFrequencies =
                List.of("ANNUAL", "SEMI_ANNUAL", "QUARTERLY", "MONTHLY");
        private Boolean allowInstallments;
        private Integer maxInstallments;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClaimsConfig {
        private Integer notificationDeadlineHours;
        private Boolean requirePoliceReport;
        @Builder.Default
        private List<String> allowedClaimTypes = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CoverageConfig {
        private String coverageCode;
        private String coverageNameAr;
        private String coverageNameEn;
        private BigDecimal coverageLimit;
        private BigDecimal deductibleAmount;
        private String descriptionAr;
        private String descriptionEn;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExclusionConfig {
        private String exclusionCode;
        private String exclusionNameAr;
        private String exclusionNameEn;
        private String descriptionAr;
        private String descriptionEn;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RatingFactorConfig {
        private String factorCode;
        private String factorName;
        private String factorType;
        private String calculationMethod;
        @Builder.Default
        private Map<String, Object> parameters = new HashMap<>();
        private Boolean isActive;
        private Integer displayOrder;
    }
}
