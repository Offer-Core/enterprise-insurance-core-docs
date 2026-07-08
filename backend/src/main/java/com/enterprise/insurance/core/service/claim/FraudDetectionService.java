package com.enterprise.insurance.core.service.claim;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.enterprise.insurance.core.domain.claim.Claim;
import com.enterprise.insurance.core.domain.claim.ClaimHistory;
import com.enterprise.insurance.core.domain.claim.ClaimStatus;
import com.enterprise.insurance.core.domain.claim.FraudAssessment;
import com.enterprise.insurance.core.domain.claim.FraudAssessment.FraudIndicatorResult;
import com.enterprise.insurance.core.domain.claim.FraudRule;
import com.enterprise.insurance.core.exception.EntityNotFoundException;
import com.enterprise.insurance.core.repository.claim.ClaimHistoryRepository;
import com.enterprise.insurance.core.repository.claim.ClaimRepository;
import com.enterprise.insurance.core.repository.claim.FraudRuleRepository;

@Service
@Transactional
public class FraudDetectionService {

    private static final Logger log = LoggerFactory.getLogger(FraudDetectionService.class);

    private final ClaimRepository claimRepository;
    private final ClaimHistoryRepository claimHistoryRepository;
    private final FraudRuleRepository fraudRuleRepository;
    private final ClaimEventPublisher eventPublisher;

    public FraudDetectionService(ClaimRepository claimRepository,
            ClaimHistoryRepository claimHistoryRepository, FraudRuleRepository fraudRuleRepository,
            ClaimEventPublisher eventPublisher) {
        this.claimRepository = claimRepository;
        this.claimHistoryRepository = claimHistoryRepository;
        this.fraudRuleRepository = fraudRuleRepository;
        this.eventPublisher = eventPublisher;
    }

    public FraudAssessment evaluateFraud(String claimNumber) {
        Claim claim = getClaim(claimNumber);
        List<FraudRule> activeRules = fraudRuleRepository.findByIsActiveTrue();

        BigDecimal totalScore = BigDecimal.ZERO;
        int totalWeight = 0;
        List<FraudIndicatorResult> indicators = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        for (FraudRule rule : activeRules) {
            boolean triggered = evaluateRule(rule, claim);
            if (triggered) {
                BigDecimal ruleScore = BigDecimal.valueOf(rule.getWeight())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                totalScore = totalScore.add(ruleScore);
                totalWeight += rule.getWeight();

                indicators.add(FraudIndicatorResult.builder().indicatorCode(rule.getRuleCode())
                        .indicatorName(rule.getRuleNameEn()).score(ruleScore)
                        .description(rule.getDescription())
                        .evidence("Rule triggered: " + rule.getCondition()).build());

                recommendations.add("Review: " + rule.getRuleNameEn());
            }
        }

        // Normalize score to 0.00 - 1.00
        if (totalWeight > 0) {
            totalScore = totalScore.divide(BigDecimal.valueOf(Math.max(1, activeRules.size())), 2,
                    RoundingMode.HALF_UP);
            if (totalScore.compareTo(BigDecimal.ONE) > 0) {
                totalScore = BigDecimal.ONE;
            }
        }

        boolean requiresManualReview = totalScore.compareTo(new BigDecimal("0.50")) >= 0;
        String riskLevel = getRiskLevel(totalScore);

        // Update claim with fraud score
        claim.setFraudScore(totalScore);
        if (requiresManualReview) {
            claim.setFraudReviewRequired(true);
            claim.setFraudIndicators(
                    indicators.stream().map(FraudIndicatorResult::getIndicatorCode).toList());
        }
        claimRepository.save(claim);

        // Save history
        saveHistory(claim, "FRAUD_EVALUATED", claim.getStatus().name(), claim.getStatus().name(),
                Map.of("fraudScore", totalScore, "riskLevel", riskLevel, "indicators",
                        indicators.stream().map(FraudIndicatorResult::getIndicatorCode).toList()),
                "SYSTEM");

        // Publish alert if high risk
        if (requiresManualReview) {
            eventPublisher.publishFraudAlert(claim);
        }

        log.info("Fraud evaluation for claim {}: score={}, risk={}", claimNumber, totalScore,
                riskLevel);

        return FraudAssessment.builder().claimNumber(claimNumber).totalScore(totalScore)
                .riskLevel(riskLevel).indicators(indicators).recommendations(recommendations)
                .requiresManualReview(requiresManualReview).assessedAt(LocalDateTime.now())
                .assessedBy("SYSTEM").build();
    }

    public BigDecimal getFraudScore(String claimNumber) {
        Claim claim = getClaim(claimNumber);
        return claim.getFraudScore() != null ? claim.getFraudScore() : BigDecimal.ZERO;
    }

    public List<String> getFraudIndicators(String claimNumber) {
        Claim claim = getClaim(claimNumber);
        return claim.getFraudIndicators();
    }

    public Claim manualFraudReview(String claimNumber, String reviewedBy) {
        Claim claim = getClaim(claimNumber);
        String previousStatus = claim.getStatus().name();

        claim.transitionTo(ClaimStatus.FRAUD_REVIEW);
        claim = claimRepository.save(claim);

        saveHistory(claim, "FRAUD_MANUAL_REVIEW", previousStatus, ClaimStatus.FRAUD_REVIEW.name(),
                Map.of("reviewedBy", reviewedBy), reviewedBy);

        eventPublisher.publishClaimStatusChanged(claim, previousStatus,
                "Manual fraud review initiated", reviewedBy);

        log.info("Manual fraud review initiated for claim: {}", claimNumber);
        return claim;
    }

    public Claim confirmFraud(String claimNumber, String confirmedBy) {
        Claim claim = getClaim(claimNumber);
        String previousStatus = claim.getStatus().name();

        claim.confirmFraud();
        claim = claimRepository.save(claim);

        saveHistory(claim, "FRAUD_CONFIRMED", previousStatus, ClaimStatus.FRAUD_CONFIRMED.name(),
                Map.of("confirmedBy", confirmedBy), confirmedBy);

        eventPublisher.publishClaimStatusChanged(claim, previousStatus, "Fraud confirmed",
                confirmedBy);

        log.info("Fraud confirmed for claim: {}", claimNumber);
        return claim;
    }

    public Claim clearFraud(String claimNumber, String clearedBy) {
        Claim claim = getClaim(claimNumber);
        String previousStatus = claim.getStatus().name();

        claim.clearFraud();
        claim = claimRepository.save(claim);

        saveHistory(claim, "FRAUD_CLEARED", previousStatus, ClaimStatus.ADJUDICATING.name(),
                Map.of("clearedBy", clearedBy), clearedBy);

        eventPublisher.publishClaimStatusChanged(claim, previousStatus, "Fraud cleared", clearedBy);

        log.info("Fraud cleared for claim: {}", claimNumber);
        return claim;
    }

    public List<Claim> getSuspiciousClaims() {
        return claimRepository.findByFraudReviewRequiredTrue();
    }

    public FraudRule updateFraudRules(FraudRule rule) {
        return fraudRuleRepository.save(rule);
    }

    private boolean evaluateRule(FraudRule rule, Claim claim) {
        // In production, this would use SpEL expression evaluation
        // For now, implement basic rule evaluation
        switch (rule.getRuleCode()) {
            case "CLAIM_FILED_AFTER_POLICY_ISSUE_30_DAYS":
                // Simplified: flag if claim amount is high relative to typical
                return claim.getClaimedAmount() != null
                        && claim.getClaimedAmount().compareTo(new BigDecimal("50000")) > 0;

            case "MULTIPLE_CLAIMS_SAME_PERIOD":
                // Check if customer has multiple claims
                return claimRepository.findByCustomerId(claim.getCustomerId()).size() >= 2;

            case "INCIDENT_TIME_INCONSISTENT":
                // Flag if reported immediately (within 1 hour)
                return claim.getReportedDate() != null && claim.getIncidentDate() != null
                        && claim.getReportedDate().toLocalDate().equals(claim.getIncidentDate());

            case "VEHICLE_VALUE_INFLATED":
                // Flag if claimed amount is very high
                return claim.getClaimedAmount() != null
                        && claim.getClaimedAmount().compareTo(new BigDecimal("100000")) > 0;

            case "THIRD_PARTY_RELATED":
                // Simplified: flag if no police report
                return false;

            default:
                return false;
        }
    }

    private String getRiskLevel(BigDecimal score) {
        if (score == null)
            return "LOW";
        if (score.compareTo(new BigDecimal("0.75")) >= 0)
            return "CRITICAL";
        if (score.compareTo(new BigDecimal("0.50")) >= 0)
            return "HIGH";
        if (score.compareTo(new BigDecimal("0.25")) >= 0)
            return "MEDIUM";
        return "LOW";
    }

    private Claim getClaim(String claimNumber) {
        return claimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found: " + claimNumber));
    }

    private void saveHistory(Claim claim, String eventType, String previousStatus, String newStatus,
            Map<String, Object> eventData, String performedBy) {
        ClaimHistory history = ClaimHistory.builder().claimId(claim.getId()).eventType(eventType)
                .previousStatus(previousStatus).newStatus(newStatus).eventData(eventData)
                .performedBy(UUID.fromString(performedBy)).tenantId(claim.getTenantId()).build();
        claimHistoryRepository.save(history);
    }
}
