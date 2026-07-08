package com.enterprise.insurance.core.api.policy;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.enterprise.insurance.core.domain.Policy;
import com.enterprise.insurance.core.domain.PolicyStatus;
import com.enterprise.insurance.core.dto.policy.PolicyResponse;
import com.enterprise.insurance.core.service.search.PolicySearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Policy Search", description = "Policy search and listing APIs")
public class PolicySearchController {

    private final PolicySearchService policySearchService;

    @GetMapping("/search")
    @Operation(summary = "Search policies",
            description = "Search policies with multiple optional filters")
    public ResponseEntity<Page<PolicyResponse>> searchPolicies(
            @RequestParam(required = false) String policyNumber,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) PolicyStatus status,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) LocalDate effectiveFrom,
            @RequestParam(required = false) LocalDate effectiveTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Policy> policies = policySearchService.searchPolicies(policyNumber, customerId, status,
                productCode, effectiveFrom, effectiveTo, page, size);
        return ResponseEntity.ok(policies.map(this::mapToResponse));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active policies")
    public ResponseEntity<List<PolicyResponse>> getActivePolicies() {
        List<Policy> policies = policySearchService.getActivePolicies();
        return ResponseEntity.ok(policies.stream().map(this::mapToResponse).toList());
    }

    @GetMapping("/expiring")
    @Operation(summary = "Get expiring policies",
            description = "Get policies expiring within a date range")
    public ResponseEntity<List<PolicyResponse>> getExpiringPolicies(@RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        List<Policy> policies = policySearchService.getExpiringPolicies(from, to);
        return ResponseEntity.ok(policies.stream().map(this::mapToResponse).toList());
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent policies")
    public ResponseEntity<List<PolicyResponse>> getRecentPolicies(
            @RequestParam(defaultValue = "30") int daysBack,
            @RequestParam(defaultValue = "10") int limit) {
        List<Policy> policies = policySearchService.getRecentPolicies(daysBack, limit);
        return ResponseEntity.ok(policies.stream().map(this::mapToResponse).toList());
    }

    @GetMapping("/by-customer")
    @Operation(summary = "Get policies by customer")
    public ResponseEntity<List<PolicyResponse>> getPoliciesByCustomer(
            @RequestParam UUID customerId) {
        List<Policy> policies = policySearchService.getPoliciesByCustomer(customerId);
        return ResponseEntity.ok(policies.stream().map(this::mapToResponse).toList());
    }

    @GetMapping("/by-status")
    @Operation(summary = "Get policies by status")
    public ResponseEntity<List<PolicyResponse>> getPoliciesByStatus(
            @RequestParam PolicyStatus status) {
        List<Policy> policies = policySearchService.getPoliciesByStatus(status);
        return ResponseEntity.ok(policies.stream().map(this::mapToResponse).toList());
    }

    @GetMapping("/by-product")
    @Operation(summary = "Get policies by product code")
    public ResponseEntity<List<PolicyResponse>> getPoliciesByProduct(
            @RequestParam String productCode) {
        List<Policy> policies = policySearchService.getPoliciesByProduct(productCode);
        return ResponseEntity.ok(policies.stream().map(this::mapToResponse).toList());
    }

    private PolicyResponse mapToResponse(Policy policy) {
        return PolicyResponse.builder().policyNumber(policy.getPolicyNumber())
                .productCode(policy.getProductCode())
                .customerId(policy.getCustomer() != null ? policy.getCustomer().getId() : null)
                .customerName(
                        policy.getCustomer() != null ? policy.getCustomer().getFullNameEn() : null)
                .status(policy.getStatus().name()).annualPremium(policy.getAnnualPremium())
                .taxAmount(policy.getTaxAmount()).totalPremium(policy.getTotalPremium())
                .currency(policy.getCurrency()).effectiveDate(policy.getEffectiveDate())
                .expiryDate(policy.getExpiryDate()).policyDocumentUrl(policy.getPolicyDocumentUrl())
                .issuedAt(policy.getIssuedAt()).activatedAt(policy.getActivatedAt())
                .createdAt(policy.getCreatedAt()).renewalCount(policy.getRenewalCount()).build();
    }
}
