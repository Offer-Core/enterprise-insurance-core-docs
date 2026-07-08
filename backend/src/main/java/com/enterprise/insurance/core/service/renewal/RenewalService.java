package com.enterprise.insurance.core.service.renewal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.enterprise.insurance.core.domain.Policy;
import com.enterprise.insurance.core.domain.PolicyStatus;
import com.enterprise.insurance.core.dto.renewal.RenewalResponse;
import com.enterprise.insurance.core.exception.BusinessRuleException;
import com.enterprise.insurance.core.exception.EntityNotFoundException;
import com.enterprise.insurance.core.repository.PolicyRepository;
import com.enterprise.insurance.core.service.policy.PolicyLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RenewalService {

    private final PolicyRepository policyRepository;
    private final PolicyLifecycleService policyLifecycleService;

    /**
     * Processes a policy renewal.
     */
    @Transactional
    public RenewalResponse renewPolicy(String policyNumber, BigDecimal newPremium) {
        Policy oldPolicy = policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new EntityNotFoundException("Policy", policyNumber));

        if (oldPolicy.getStatus() != PolicyStatus.ACTIVE
                && oldPolicy.getStatus() != PolicyStatus.ISSUED) {
            throw new BusinessRuleException("POLICY_NOT_ACTIVE",
                    "Only active policies can be renewed");
        }

        // Create new policy for renewal
        Policy newPolicy = Policy.builder().policyNumber(generateRenewalPolicyNumber(policyNumber))
                .productCode(oldPolicy.getProductCode())
                .lineOfBusiness(oldPolicy.getLineOfBusiness()).customer(oldPolicy.getCustomer())
                .status(PolicyStatus.ACTIVE).effectiveDate(oldPolicy.getExpiryDate().plusDays(1))
                .expiryDate(oldPolicy.getExpiryDate().plusDays(1).plusYears(1))
                .annualPremium(newPremium).taxAmount(calculateTax(newPremium))
                .totalPremium(newPremium.add(calculateTax(newPremium)))
                .currency(oldPolicy.getCurrency()).agentId(oldPolicy.getAgentId())
                .tenantId(oldPolicy.getTenantId()).build();

        Policy saved = policyLifecycleService.renewPolicy(policyNumber, newPolicy);

        BigDecimal premiumChange = newPremium.subtract(oldPolicy.getAnnualPremium());
        BigDecimal premiumChangePercent =
                oldPolicy.getAnnualPremium().compareTo(BigDecimal.ZERO) > 0
                        ? premiumChange.multiply(BigDecimal.valueOf(100))
                                .divide(oldPolicy.getAnnualPremium(), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

        List<String> changes = new ArrayList<>();
        changes.add("Premium: " + oldPolicy.getAnnualPremium() + " -> " + newPremium);
        if (premiumChange.compareTo(BigDecimal.ZERO) > 0) {
            changes.add("Premium increase of " + premiumChangePercent + "%");
        } else if (premiumChange.compareTo(BigDecimal.ZERO) < 0) {
            changes.add("Premium decrease of " + premiumChangePercent.abs() + "%");
        }

        log.info("Policy renewed: {} -> {} premium: {} -> {}", policyNumber,
                saved.getPolicyNumber(), oldPolicy.getAnnualPremium(), newPremium);

        return RenewalResponse.builder().oldPolicyNumber(policyNumber)
                .newPolicyNumber(saved.getPolicyNumber())
                .previousPremium(oldPolicy.getAnnualPremium()).newPremium(newPremium)
                .premiumChange(premiumChange).premiumChangePercent(premiumChangePercent)
                .newEffectiveDate(saved.getEffectiveDate()).newExpiryDate(saved.getExpiryDate())
                .status("RENEWED").changes(changes).build();
    }

    /**
     * Finds policies expiring within a date range for renewal processing.
     */
    public List<Policy> findPoliciesForRenewal(LocalDate from, LocalDate to) {
        return policyRepository.findByExpiryDateBetweenAndStatus(from, to, PolicyStatus.ACTIVE);
    }

    /**
     * Calculates VAT (15% in Saudi Arabia).
     */
    private BigDecimal calculateTax(BigDecimal premium) {
        return premium.multiply(BigDecimal.valueOf(0.15)).setScale(2, RoundingMode.HALF_UP);
    }

    private String generateRenewalPolicyNumber(String oldPolicyNumber) {
        return oldPolicyNumber + "-R" + System.currentTimeMillis();
    }
}
