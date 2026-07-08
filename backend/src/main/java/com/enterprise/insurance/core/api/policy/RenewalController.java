package com.enterprise.insurance.core.api.policy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.enterprise.insurance.core.domain.Policy;
import com.enterprise.insurance.core.dto.policy.PolicyResponse;
import com.enterprise.insurance.core.dto.renewal.RenewalResponse;
import com.enterprise.insurance.core.service.renewal.RenewalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/renewals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Renewals", description = "Policy renewal APIs")
public class RenewalController {

    private final RenewalService renewalService;

    @PostMapping("/{policyNumber}")
    @Operation(summary = "Renew a policy",
            description = "Processes policy renewal with new premium")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Policy renewed successfully"),
            @ApiResponse(responseCode = "400", description = "Policy not eligible for renewal"),
            @ApiResponse(responseCode = "404", description = "Policy not found")})
    public ResponseEntity<RenewalResponse> renewPolicy(
            @Parameter(description = "Policy number") @PathVariable String policyNumber,
            @Valid @RequestBody RenewalRequest request) {
        RenewalResponse response = renewalService.renewPolicy(policyNumber, request.newPremium());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/eligible")
    @Operation(summary = "Find policies eligible for renewal",
            description = "Finds active policies expiring within a date range")
    public ResponseEntity<List<PolicyResponse>> findEligiblePolicies(@RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        List<Policy> policies = renewalService.findPoliciesForRenewal(from, to);
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
                .expiryDate(policy.getExpiryDate()).renewalCount(policy.getRenewalCount()).build();
    }

    public record RenewalRequest(BigDecimal newPremium) {
    }
}
