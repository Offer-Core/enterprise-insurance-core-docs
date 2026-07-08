package com.enterprise.insurance.core.service.policy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.enterprise.insurance.core.domain.Policy;
import com.enterprise.insurance.core.domain.PolicyStatus;
import com.enterprise.insurance.core.event.policy.PolicyActivatedEvent;
import com.enterprise.insurance.core.event.policy.PolicyCancelledEvent;
import com.enterprise.insurance.core.event.policy.PolicyCreatedEvent;
import com.enterprise.insurance.core.event.policy.PolicyExpiredEvent;
import com.enterprise.insurance.core.event.policy.PolicyIssuedEvent;
import com.enterprise.insurance.core.event.policy.PolicyRenewedEvent;
import com.enterprise.insurance.core.exception.BusinessRuleException;
import com.enterprise.insurance.core.exception.EntityNotFoundException;
import com.enterprise.insurance.core.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages the complete lifecycle of a policy through its state machine.
 *
 * Lifecycle: QUOTE → BOUND → ACTIVE → ISSUED → RENEWED → EXPIRED → CANCELLED → LAPSED → SUSPENDED →
 * ENDORSED
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyLifecycleService {

    private final PolicyRepository policyRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Creates a new policy in QUOTE status.
     */
    @Transactional
    public Policy createPolicy(Policy policy) {
        policy.setStatus(PolicyStatus.QUOTE);
        Policy saved = policyRepository.save(policy);

        eventPublisher.publishEvent(new PolicyCreatedEvent(UUID.randomUUID(),
                saved.getPolicyNumber(), saved.getProductCode(), saved.getCustomer().getId(),
                saved.getAnnualPremium(), saved.getEffectiveDate(), saved.getExpiryDate(),
                saved.getLineOfBusiness().name(), saved.getTenantId(), null));

        log.info("Policy created: {} in status QUOTE", saved.getPolicyNumber());
        return saved;
    }

    /**
     * Binds a quote to a policy (customer accepts the quote).
     */
    @Transactional
    public Policy bindPolicy(String policyNumber) {
        Policy policy = findPolicyByNumber(policyNumber);
        validateTransition(policy, PolicyStatus.BOUND);

        policy.setStatus(PolicyStatus.BOUND);
        policy.setBoundAt(LocalDateTime.now());
        Policy saved = policyRepository.save(policy);

        log.info("Policy bound: {}", saved.getPolicyNumber());
        return saved;
    }

    /**
     * Activates a policy (payment received, coverage starts).
     */
    @Transactional
    public Policy activatePolicy(String policyNumber) {
        Policy policy = findPolicyByNumber(policyNumber);
        validateTransition(policy, PolicyStatus.ACTIVE);

        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setActivatedAt(LocalDateTime.now());
        Policy saved = policyRepository.save(policy);

        eventPublisher.publishEvent(new PolicyActivatedEvent(UUID.randomUUID(),
                saved.getPolicyNumber(), LocalDate.now(), null));

        log.info("Policy activated: {}", saved.getPolicyNumber());
        return saved;
    }

    /**
     * Issues a policy document.
     */
    @Transactional
    public Policy issuePolicy(String policyNumber, String documentUrl) {
        Policy policy = findPolicyByNumber(policyNumber);
        validateTransition(policy, PolicyStatus.ISSUED);

        policy.setStatus(PolicyStatus.ISSUED);
        policy.setPolicyDocumentUrl(documentUrl);
        policy.setIssuedAt(LocalDateTime.now());
        Policy saved = policyRepository.save(policy);

        eventPublisher
                .publishEvent(new PolicyIssuedEvent(UUID.randomUUID(), saved.getPolicyNumber(),
                        saved.getProductCode(), documentUrl, LocalDate.now(), null));

        log.info("Policy issued: {} with document: {}", saved.getPolicyNumber(), documentUrl);
        return saved;
    }

    /**
     * Cancels a policy with pro-rata refund calculation.
     */
    @Transactional
    public Policy cancelPolicy(String policyNumber, String reason, String cancellationCode) {
        Policy policy = findPolicyByNumber(policyNumber);
        validateTransition(policy, PolicyStatus.CANCELLED);

        BigDecimal refundAmount = calculateRefund(policy);

        policy.setStatus(PolicyStatus.CANCELLED);
        policy.setCancelledAt(LocalDateTime.now());
        policy.setCancellationReason(reason);
        policy.setCancellationCode(cancellationCode);
        Policy saved = policyRepository.save(policy);

        eventPublisher.publishEvent(new PolicyCancelledEvent(UUID.randomUUID(),
                saved.getPolicyNumber(), reason, LocalDate.now(), refundAmount, null));

        log.info("Policy cancelled: {} reason: {} refund: {}", saved.getPolicyNumber(), reason,
                refundAmount);
        return saved;
    }

    /**
     * Renews a policy for a new term.
     */
    @Transactional
    public Policy renewPolicy(String oldPolicyNumber, Policy newPolicy) {
        Policy oldPolicy = findPolicyByNumber(oldPolicyNumber);
        validateTransition(oldPolicy, PolicyStatus.RENEWED);

        // Mark old policy as renewed
        oldPolicy.setStatus(PolicyStatus.RENEWED);
        oldPolicy.setRenewedAt(LocalDateTime.now());
        oldPolicy.setRenewalCount(oldPolicy.getRenewalCount() + 1);
        policyRepository.save(oldPolicy);

        // Create new policy
        newPolicy.setStatus(PolicyStatus.ACTIVE);
        newPolicy.setRenewalCount(0);
        newPolicy.setPreviousPolicyNumber(oldPolicyNumber);
        Policy saved = policyRepository.save(newPolicy);

        eventPublisher.publishEvent(new PolicyRenewedEvent(UUID.randomUUID(), oldPolicyNumber,
                saved.getPolicyNumber(), saved.getAnnualPremium(), saved.getEffectiveDate(),
                saved.getExpiryDate(), null));

        log.info("Policy renewed: {} -> {}", oldPolicyNumber, saved.getPolicyNumber());
        return saved;
    }

    /**
     * Marks a policy as expired.
     */
    @Transactional
    public Policy expirePolicy(String policyNumber) {
        Policy policy = findPolicyByNumber(policyNumber);
        validateTransition(policy, PolicyStatus.EXPIRED);

        policy.setStatus(PolicyStatus.EXPIRED);
        Policy saved = policyRepository.save(policy);

        eventPublisher.publishEvent(
                new PolicyExpiredEvent(UUID.randomUUID(), saved.getPolicyNumber(), null));

        log.info("Policy expired: {}", saved.getPolicyNumber());
        return saved;
    }

    /**
     * Suspends a policy (e.g., for non-payment).
     */
    @Transactional
    public Policy suspendPolicy(String policyNumber, String reason) {
        Policy policy = findPolicyByNumber(policyNumber);
        validateTransition(policy, PolicyStatus.SUSPENDED);

        policy.setStatus(PolicyStatus.SUSPENDED);
        policy.setSuspendedAt(LocalDateTime.now());
        policy.setSuspensionReason(reason);
        Policy saved = policyRepository.save(policy);

        log.info("Policy suspended: {} reason: {}", saved.getPolicyNumber(), reason);
        return saved;
    }

    /**
     * Reactivates a suspended policy.
     */
    @Transactional
    public Policy reactivatePolicy(String policyNumber) {
        Policy policy = findPolicyByNumber(policyNumber);
        if (policy.getStatus() != PolicyStatus.SUSPENDED) {
            throw new BusinessRuleException("POLICY_NOT_SUSPENDED",
                    "Only suspended policies can be reactivated");
        }

        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setSuspendedAt(null);
        policy.setSuspensionReason(null);
        Policy saved = policyRepository.save(policy);

        log.info("Policy reactivated: {}", saved.getPolicyNumber());
        return saved;
    }

    /**
     * Calculates pro-rata refund for cancellation.
     */
    private BigDecimal calculateRefund(Policy policy) {
        if (policy.getStatus() == PolicyStatus.QUOTE || policy.getStatus() == PolicyStatus.BOUND) {
            return policy.getTotalPremium();
        }

        long totalDays = ChronoUnit.DAYS.between(policy.getEffectiveDate(), policy.getExpiryDate());
        long usedDays = ChronoUnit.DAYS.between(policy.getEffectiveDate(), LocalDate.now());

        if (usedDays >= totalDays) {
            return BigDecimal.ZERO;
        }

        BigDecimal dailyRate = policy.getTotalPremium().divide(BigDecimal.valueOf(totalDays), 10,
                RoundingMode.HALF_UP);
        long remainingDays = totalDays - usedDays;
        BigDecimal refund = dailyRate.multiply(BigDecimal.valueOf(remainingDays));

        // Apply cancellation penalty (10% of refund)
        BigDecimal penalty = refund.multiply(BigDecimal.valueOf(0.10));
        return refund.subtract(penalty).setScale(2, RoundingMode.HALF_UP);
    }

    private Policy findPolicyByNumber(String policyNumber) {
        return policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new EntityNotFoundException("Policy", policyNumber));
    }

    private void validateTransition(Policy policy, PolicyStatus targetStatus) {
        if (!policy.getStatus().canTransitionTo(targetStatus)) {
            throw new BusinessRuleException("INVALID_STATUS_TRANSITION",
                    String.format("Cannot transition policy %s from %s to %s",
                            policy.getPolicyNumber(), policy.getStatus(), targetStatus));
        }
    }
}
