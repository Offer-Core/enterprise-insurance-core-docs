package com.enterprise.insurance.core.service.claim;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.enterprise.insurance.core.domain.claim.AdjudicationResult;
import com.enterprise.insurance.core.domain.claim.AdjudicationResult.AdjudicationDecision;
import com.enterprise.insurance.core.domain.claim.Claim;
import com.enterprise.insurance.core.domain.claim.ClaimHistory;
import com.enterprise.insurance.core.domain.claim.ClaimStatus;
import com.enterprise.insurance.core.domain.claim.SettlementCalculation;
import com.enterprise.insurance.core.domain.claim.SettlementCalculation.SettlementComponent;
import com.enterprise.insurance.core.dto.claim.ApprovalRequest;
import com.enterprise.insurance.core.dto.claim.RejectionRequest;
import com.enterprise.insurance.core.exception.EntityNotFoundException;
import com.enterprise.insurance.core.repository.claim.ClaimHistoryRepository;
import com.enterprise.insurance.core.repository.claim.ClaimRepository;

@Service
@Transactional
public class ClaimAdjudicationService {

    private static final Logger log = LoggerFactory.getLogger(ClaimAdjudicationService.class);

    private final ClaimRepository claimRepository;
    private final ClaimHistoryRepository claimHistoryRepository;
    private final ClaimEventPublisher eventPublisher;

    public ClaimAdjudicationService(ClaimRepository claimRepository,
            ClaimHistoryRepository claimHistoryRepository, ClaimEventPublisher eventPublisher) {
        this.claimRepository = claimRepository;
        this.claimHistoryRepository = claimHistoryRepository;
        this.eventPublisher = eventPublisher;
    }

    public AdjudicationResult evaluateClaim(String claimNumber) {
        Claim claim = getClaim(claimNumber);

        // Basic coverage validation
        boolean hasCoverage = validateCoverage(claim);

        if (!hasCoverage) {
            return AdjudicationResult.builder().claimNumber(claimNumber)
                    .decision(AdjudicationDecision.REJECTED).approvedAmount(BigDecimal.ZERO)
                    .rejectedAmount(claim.getClaimedAmount())
                    .reason("No coverage for this claim type").decisionDate(LocalDate.now())
                    .build();
        }

        // Calculate settlement
        SettlementCalculation calc = calculateSettlementAmount(claim);

        return AdjudicationResult.builder().claimNumber(claimNumber)
                .decision(calc.getNetPayableAmount().compareTo(BigDecimal.ZERO) > 0
                        ? AdjudicationDecision.APPROVED
                        : AdjudicationDecision.REJECTED)
                .approvedAmount(calc.getNetPayableAmount())
                .rejectedAmount(claim.getClaimedAmount().subtract(calc.getNetPayableAmount()))
                .deductibleApplied(calc.getDeductibleAmount()).excessApplied(calc.getExcessAmount())
                .decisionDate(LocalDate.now()).build();
    }

    public Claim approveClaim(String claimNumber, ApprovalRequest request) {
        Claim claim = getClaim(claimNumber);
        String previousStatus = claim.getStatus().name();

        claim.approve(request.getApprovedAmount());
        claim = claimRepository.save(claim);

        saveHistory(claim, "CLAIM_APPROVED", previousStatus, ClaimStatus.APPROVED.name(),
                Map.of("approvedAmount", request.getApprovedAmount(), "reason", request.getReason(),
                        "conditions", request.getConditions(), "notes", request.getNotes()),
                request.getApprovedBy());

        eventPublisher.publishClaimApproved(claim, request.getApprovedBy(), request.getReason());
        eventPublisher.publishClaimStatusChanged(claim, previousStatus, "Claim approved",
                request.getApprovedBy());

        log.info("Claim {} approved for amount: {}", claimNumber, request.getApprovedAmount());
        return claim;
    }

    public Claim partialApproveClaim(String claimNumber, ApprovalRequest request) {
        Claim claim = getClaim(claimNumber);
        String previousStatus = claim.getStatus().name();

        claim.partialApprove(request.getApprovedAmount());
        claim = claimRepository.save(claim);

        saveHistory(claim, "CLAIM_PARTIAL_APPROVED", previousStatus,
                ClaimStatus.PARTIAL_APPROVED.name(),
                Map.of("approvedAmount", request.getApprovedAmount(), "reason", request.getReason(),
                        "conditions", request.getConditions()),
                request.getApprovedBy());

        eventPublisher.publishClaimStatusChanged(claim, previousStatus, "Claim partially approved",
                request.getApprovedBy());

        log.info("Claim {} partially approved for amount: {}", claimNumber,
                request.getApprovedAmount());
        return claim;
    }

    public Claim rejectClaim(String claimNumber, RejectionRequest request) {
        Claim claim = getClaim(claimNumber);
        String previousStatus = claim.getStatus().name();

        claim.reject();
        claim = claimRepository.save(claim);

        saveHistory(claim, "CLAIM_REJECTED", previousStatus, ClaimStatus.REJECTED.name(),
                Map.of("reason", request.getReason(), "rejectionCode", request.getRejectionCode(),
                        "notes", request.getNotes()),
                request.getRejectedBy());

        eventPublisher.publishClaimRejected(claim, request.getRejectedBy(), request.getReason());
        eventPublisher.publishClaimStatusChanged(claim, previousStatus,
                "Claim rejected: " + request.getReason(), request.getRejectedBy());

        log.info("Claim {} rejected. Reason: {}", claimNumber, request.getReason());
        return claim;
    }

    public boolean validateCoverage(Claim claim) {
        // Basic coverage validation logic
        // In production, this would check policy coverage, exclusions, waiting periods
        if (claim.getClaimedAmount() == null
                || claim.getClaimedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return true;
    }

    public SettlementCalculation calculateSettlementAmount(Claim claim) {
        BigDecimal deductible =
                claim.getExcessAmount() != null ? claim.getExcessAmount() : BigDecimal.ZERO;
        BigDecimal excess = BigDecimal.ZERO;

        // Apply deductible
        BigDecimal afterDeductible = claim.getClaimedAmount().subtract(deductible);
        if (afterDeductible.compareTo(BigDecimal.ZERO) < 0) {
            afterDeductible = BigDecimal.ZERO;
        }

        // Apply excess percentage if applicable
        if ("PERCENTAGE".equals(claim.getExcessType()) && claim.getExcessAmount() != null) {
            excess = claim.getClaimedAmount().multiply(claim.getExcessAmount())
                    .divide(new BigDecimal("100"));
            afterDeductible = afterDeductible.subtract(excess);
            if (afterDeductible.compareTo(BigDecimal.ZERO) < 0) {
                afterDeductible = BigDecimal.ZERO;
            }
        }

        BigDecimal netPayable = afterDeductible;

        return SettlementCalculation.builder().totalApprovedAmount(netPayable)
                .deductibleAmount(deductible).excessAmount(excess).salvageAmount(BigDecimal.ZERO)
                .netPayableAmount(netPayable)
                .components(List.of(
                        SettlementComponent.builder().componentType("CLAIMED_AMOUNT")
                                .description("Original claimed amount")
                                .amount(claim.getClaimedAmount()).build(),
                        SettlementComponent.builder().componentType("DEDUCTIBLE")
                                .description("Policy deductible applied")
                                .amount(deductible.negate()).build(),
                        SettlementComponent.builder().componentType("NET_PAYABLE")
                                .description("Net payable amount").amount(netPayable).build()))
                .build();
    }

    public BigDecimal applyDeductible(BigDecimal amount, Claim claim) {
        if (claim.getExcessAmount() != null) {
            BigDecimal deductible = claim.getExcessAmount();
            BigDecimal result = amount.subtract(deductible);
            return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
        }
        return amount;
    }

    public List<ClaimHistory> getAdjudicationHistory(String claimNumber) {
        Claim claim = getClaim(claimNumber);
        return claimHistoryRepository.findByClaimIdOrderByOccurredAtAsc(claim.getId()).stream()
                .filter(h -> h.getEventType().contains("ADJUDICAT")
                        || h.getEventType().contains("APPROV")
                        || h.getEventType().contains("REJECT"))
                .toList();
    }

    public Claim requireFraudReview(String claimNumber, List<String> indicators,
            String performedBy) {
        Claim claim = getClaim(claimNumber);
        String previousStatus = claim.getStatus().name();

        claim.flagForFraudReview(indicators);
        claim = claimRepository.save(claim);

        saveHistory(claim, "FRAUD_REVIEW_REQUIRED", previousStatus, ClaimStatus.FRAUD_REVIEW.name(),
                Map.of("indicators", indicators), performedBy);

        eventPublisher.publishClaimStatusChanged(claim, previousStatus, "Fraud review required",
                performedBy);

        log.info("Fraud review required for claim: {} (indicators: {})", claimNumber, indicators);
        return claim;
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
