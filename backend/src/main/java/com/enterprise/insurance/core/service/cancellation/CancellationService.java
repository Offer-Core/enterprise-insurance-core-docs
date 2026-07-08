package com.enterprise.insurance.core.service.cancellation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.enterprise.insurance.core.domain.Policy;
import com.enterprise.insurance.core.domain.PolicyStatus;
import com.enterprise.insurance.core.dto.cancellation.CancellationRequest;
import com.enterprise.insurance.core.dto.cancellation.CancellationResponse;
import com.enterprise.insurance.core.exception.BusinessRuleException;
import com.enterprise.insurance.core.exception.EntityNotFoundException;
import com.enterprise.insurance.core.repository.PolicyRepository;
import com.enterprise.insurance.core.service.policy.PolicyLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancellationService {

    private final PolicyRepository policyRepository;
    private final PolicyLifecycleService policyLifecycleService;

    /**
     * Processes a policy cancellation with refund calculation.
     */
    @Transactional
    public CancellationResponse cancelPolicy(CancellationRequest request) {
        Policy policy = policyRepository.findByPolicyNumber(request.getPolicyNumber()).orElseThrow(
                () -> new EntityNotFoundException("Policy", request.getPolicyNumber()));

        if (!canCancel(policy)) {
            throw new BusinessRuleException("CANNOT_CANCEL", "Policy " + request.getPolicyNumber()
                    + " cannot be cancelled in status " + policy.getStatus());
        }

        // Calculate refund
        BigDecimal refundAmount = BigDecimal.ZERO;
        BigDecimal penaltyAmount = BigDecimal.ZERO;

        if (request.getProRataRefund()) {
            refundAmount = calculateProRataRefund(policy);
            penaltyAmount = calculatePenalty(refundAmount);
            refundAmount = refundAmount.subtract(penaltyAmount);
        }

        // Process cancellation
        policyLifecycleService.cancelPolicy(request.getPolicyNumber(),
                request.getCancellationReason(), request.getCancellationCode());

        log.info("Policy cancelled: {} refund: {} penalty: {}", request.getPolicyNumber(),
                refundAmount, penaltyAmount);

        return CancellationResponse.builder().policyNumber(request.getPolicyNumber())
                .cancellationReason(request.getCancellationReason())
                .cancellationDate(LocalDate.now())
                .refundAmount(refundAmount.setScale(2, RoundingMode.HALF_UP))
                .penaltyAmount(penaltyAmount.setScale(2, RoundingMode.HALF_UP))
                .refundMethod("ORIGINAL_PAYMENT_METHOD").status("CANCELLED")
                .message("Policy cancelled successfully").build();
    }

    /**
     * Checks if a policy can be cancelled.
     */
    public boolean canCancel(Policy policy) {
        return policy.getStatus() == PolicyStatus.QUOTE || policy.getStatus() == PolicyStatus.BOUND
                || policy.getStatus() == PolicyStatus.ACTIVE
                || policy.getStatus() == PolicyStatus.ISSUED
                || policy.getStatus() == PolicyStatus.SUSPENDED;
    }

    /**
     * Calculates pro-rata refund based on unused premium.
     */
    private BigDecimal calculateProRataRefund(Policy policy) {
        if (policy.getStatus() == PolicyStatus.QUOTE || policy.getStatus() == PolicyStatus.BOUND) {
            return policy.getTotalPremium();
        }

        long totalDays = ChronoUnit.DAYS.between(policy.getEffectiveDate(), policy.getExpiryDate());
        long usedDays = ChronoUnit.DAYS.between(policy.getEffectiveDate(), LocalDate.now());

        if (usedDays >= totalDays || totalDays <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal dailyRate = policy.getTotalPremium().divide(BigDecimal.valueOf(totalDays), 10,
                RoundingMode.HALF_UP);
        long remainingDays = totalDays - usedDays;
        return dailyRate.multiply(BigDecimal.valueOf(remainingDays));
    }

    /**
     * Calculates cancellation penalty (10% of refund).
     */
    private BigDecimal calculatePenalty(BigDecimal refundAmount) {
        return refundAmount.multiply(BigDecimal.valueOf(0.10)).setScale(2, RoundingMode.HALF_UP);
    }
}
