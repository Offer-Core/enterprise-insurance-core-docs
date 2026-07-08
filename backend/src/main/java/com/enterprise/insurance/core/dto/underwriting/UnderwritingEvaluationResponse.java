package com.enterprise.insurance.core.dto.underwriting;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnderwritingEvaluationResponse {

    private String overallStatus;
    private List<RuleEvaluationResult> ruleResults;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RuleEvaluationResult {
        private String ruleCode;
        private String ruleName;
        private String status;
        private String message;
    }
}
