package com.enterprise.insurance.core.service.endorsement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.enterprise.insurance.core.domain.Policy;
import com.enterprise.insurance.core.domain.PolicyStatus;
import com.enterprise.insurance.core.domain.endorsement.Endorsement;
import com.enterprise.insurance.core.domain.endorsement.EndorsementStatus;
import com.enterprise.insurance.core.domain.endorsement.EndorsementType;
import com.enterprise.insurance.core.dto.endorsement.EndorsementRequest;
import com.enterprise.insurance.core.event.policy.PolicyEndorsedEvent;
import com.enterprise.insurance.core.exception.BusinessRuleException;
import com.enterprise.insurance.core.exception.EntityNotFoundException;
import com.enterprise.insurance.core.repository.PolicyRepository;
import com.enterprise.insurance.core.repository.endorsement.EndorsementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EndorsementService {

    private final EndorsementRepository endorsementRepository;
    private final PolicyRepository policyRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Creates a new endorsement request for a policy.
     */
    @Transactional
    public Endorsement createEndorsement(EndorsementRequest request, UUID userId, String tenantId) {
        Policy policy = policyRepository.findByPolicyNumber(request.getPolicyNumber()).orElseThrow(
                () -> new EntityNotFoundException("Policy", request.getPolicyNumber()));

        if (!policy.getStatus().isActive()) {
            throw new BusinessRuleException("POLICY_NOT_ACTIVE",
                    "Endorsements can only be created for active policies");
        }

        // Capture current state
        Map<String, Object> priorState = capturePolicyState(policy);

        Endorsement endorsement = Endorsement.builder()
                .endorsementNumber(generateEndorsementNumber()).policyId(policy.getId())
                .endorsementType(EndorsementType.valueOf(request.getEndorsementType()))
                .description(request.getDescription()).effectiveDate(request.getEffectiveDate())
                .priorState(priorState).newState(request.getChanges())
                .status(EndorsementStatus.REQUESTED).tenantId(tenantId).createdBy(userId).build();

        Endorsement saved = endorsementRepository.save(endorsement);
        log.info("Endorsement created: {} for policy: {}", saved.getEndorsementNumber(),
                request.getPolicyNumber());
        return saved;
    }

    /**
     * Approves an endorsement request.
     */
    @Transactional
    public Endorsement approveEndorsement(String endorsementNumber, UUID approvedBy) {
        Endorsement endorsement = findEndorsementByNumber(endorsementNumber);
        endorsement.approve(approvedBy);
        Endorsement saved = endorsementRepository.save(endorsement);
        log.info("Endorsement approved: {}", saved.getEndorsementNumber());
        return saved;
    }

    /**
     * Rejects an endorsement request.
     */
    @Transactional
    public Endorsement rejectEndorsement(String endorsementNumber, UUID rejectedBy, String reason) {
        Endorsement endorsement = findEndorsementByNumber(endorsementNumber);
        endorsement.reject(rejectedBy, reason);
        Endorsement saved = endorsementRepository.save(endorsement);
        log.info("Endorsement rejected: {} reason: {}", saved.getEndorsementNumber(), reason);
        return saved;
    }

    /**
     * Applies an approved endorsement to the policy.
     */
    @Transactional
    public Endorsement applyEndorsement(String endorsementNumber) {
        Endorsement endorsement = findEndorsementByNumber(endorsementNumber);
        endorsement.apply();
        Endorsement saved = endorsementRepository.save(endorsement);

        // Update policy status to ENDORSED
        Policy policy = policyRepository.findById(endorsement.getPolicyId()).orElseThrow(
                () -> new EntityNotFoundException("Policy", endorsement.getPolicyId()));

        policy.setStatus(PolicyStatus.ENDORSED);
        policyRepository.save(policy);

        eventPublisher
                .publishEvent(new PolicyEndorsedEvent(UUID.randomUUID(), policy.getPolicyNumber(),
                        saved.getEndorsementNumber(), saved.getEndorsementType().name(),
                        saved.getPremiumAdjustment(), saved.getEffectiveDate(), null));

        log.info("Endorsement applied: {} to policy: {}", saved.getEndorsementNumber(),
                policy.getPolicyNumber());
        return saved;
    }

    public List<Endorsement> getEndorsementsByPolicy(UUID policyId) {
        return endorsementRepository.findByPolicyId(policyId);
    }

    public Endorsement findEndorsementByNumber(String endorsementNumber) {
        return endorsementRepository.findByEndorsementNumber(endorsementNumber)
                .orElseThrow(() -> new EntityNotFoundException("Endorsement", endorsementNumber));
    }

    /**
     * Captures the current state of a policy for comparison.
     */
    private Map<String, Object> capturePolicyState(Policy policy) {
        Map<String, Object> state = new HashMap<>();
        state.put("status", policy.getStatus().name());
        state.put("effectiveDate", policy.getEffectiveDate().toString());
        state.put("expiryDate", policy.getExpiryDate().toString());
        state.put("annualPremium", policy.getAnnualPremium());
        state.put("totalPremium", policy.getTotalPremium());
        state.put("currency", policy.getCurrency());
        return state;
    }

    private String generateEndorsementNumber() {
        return "END-" + System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
