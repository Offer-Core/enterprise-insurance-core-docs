package com.enterprise.insurance.core.domain.underwriting;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of evaluating a single underwriting rule.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnderwritingRuleResult {

    private String ruleCode;
    private String ruleName;
    private UnderwritingRuleStatus status;
    private String message;
    @Builder.Default
    private Map<String, Object> details = new HashMap<>();

    public enum UnderwritingRuleStatus {
        PASS, FAIL, REFERRAL
    }
}
