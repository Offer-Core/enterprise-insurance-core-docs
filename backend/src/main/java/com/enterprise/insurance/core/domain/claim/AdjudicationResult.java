package com.enterprise.insurance.core.domain.claim;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjudicationResult {
    private String claimNumber;
    private AdjudicationDecision decision;
    private BigDecimal approvedAmount;
    private BigDecimal rejectedAmount;
    private String reason;
    private List<String> conditions;
    private BigDecimal deductibleApplied;
    private BigDecimal excessApplied;
    private LocalDate decisionDate;
    private String adjudicatorId;
    private String notes;

    public enum AdjudicationDecision {
        APPROVED, PARTIAL_APPROVED, REJECTED
    }
}
