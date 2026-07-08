package com.enterprise.insurance.core.service.search;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.enterprise.insurance.core.domain.Policy;
import com.enterprise.insurance.core.domain.PolicyStatus;
import com.enterprise.insurance.core.repository.policy.PolicySearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicySearchService {

    private final PolicySearchRepository policySearchRepository;

    /**
     * Searches policies with multiple optional filters.
     */
    public Page<Policy> searchPolicies(String policyNumber, UUID customerId, PolicyStatus status,
            String productCode, LocalDate effectiveFrom, LocalDate effectiveTo, int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return policySearchRepository.searchPolicies(policyNumber, customerId, status, productCode,
                effectiveFrom, effectiveTo, pageable);
    }

    /**
     * Gets all active policies.
     */
    public List<Policy> getActivePolicies() {
        return policySearchRepository.findActivePolicies();
    }

    /**
     * Gets policies expiring within a date range.
     */
    public List<Policy> getExpiringPolicies(LocalDate from, LocalDate to) {
        return policySearchRepository.findExpiringBetween(from, to);
    }

    /**
     * Gets recent policies.
     */
    public List<Policy> getRecentPolicies(int daysBack, int limit) {
        return policySearchRepository.findRecentPolicies(
                LocalDate.now().minusDays(daysBack).atStartOfDay(), PageRequest.of(0, limit));
    }

    /**
     * Gets policies by customer.
     */
    public List<Policy> getPoliciesByCustomer(UUID customerId) {
        return policySearchRepository.findByCustomerId(customerId);
    }

    /**
     * Gets policies by status.
     */
    public List<Policy> getPoliciesByStatus(PolicyStatus status) {
        return policySearchRepository.findByStatus(status);
    }

    /**
     * Gets policies by product code.
     */
    public List<Policy> getPoliciesByProduct(String productCode) {
        return policySearchRepository.findByProductCode(productCode);
    }
}
