package com.enterprise.insurance.core.domain.claim;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudAssessment {
    private String claimNumber;
    private BigDecimal totalScore;
    private String riskLevel;
    private List<FraudIndicatorResult> indicators;
    private List<String> recommendations;
    private Boolean requiresManualReview;
    private LocalDateTime assessedAt;
    private String assessedBy;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FraudIndicatorResult {
        private String indicatorCode;
        private String indicatorName;
        private BigDecimal score;
        private String description;
        private String evidence;
    }
}
