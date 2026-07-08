package com.enterprise.insurance.core.api.policy;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.enterprise.insurance.core.domain.Policy;
import com.enterprise.insurance.core.dto.policy.PolicyResponse;
import com.enterprise.insurance.core.service.policy.PolicyLifecycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Policy Lifecycle", description = "Policy lifecycle management APIs")
public class PolicyLifecycleController {

    private final PolicyLifecycleService policyLifecycleService;

    @PostMapping("/{policyNumber}/bind")
    @Operation(summary = "Bind a policy", description = "Accepts a quote and binds the policy")
    @ApiResponses(
            value = {@ApiResponse(responseCode = "200", description = "Policy bound successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid status transition"),
                    @ApiResponse(responseCode = "404", description = "Policy not found")})
    public ResponseEntity<PolicyResponse> bindPolicy(
            @Parameter(description = "Policy number") @PathVariable String policyNumber) {
        Policy policy = policyLifecycleService.bindPolicy(policyNumber);
        return ResponseEntity.ok(mapToResponse(policy));
    }

    @PostMapping("/{policyNumber}/activate")
    @Operation(summary = "Activate a policy",
            description = "Activates policy coverage after payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Policy activated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "404", description = "Policy not found")})
    public ResponseEntity<PolicyResponse> activatePolicy(
            @Parameter(description = "Policy number") @PathVariable String policyNumber) {
        Policy policy = policyLifecycleService.activatePolicy(policyNumber);
        return ResponseEntity.ok(mapToResponse(policy));
    }

    @PostMapping("/{policyNumber}/issue")
    @Operation(summary = "Issue a policy", description = "Issues the policy document")
    @ApiResponses(
            value = {@ApiResponse(responseCode = "200", description = "Policy issued successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid status transition"),
                    @ApiResponse(responseCode = "404", description = "Policy not found")})
    public ResponseEntity<PolicyResponse> issuePolicy(
            @Parameter(description = "Policy number") @PathVariable String policyNumber,
            @RequestBody IssueRequest request) {
        Policy policy = policyLifecycleService.issuePolicy(policyNumber, request.documentUrl());
        return ResponseEntity.ok(mapToResponse(policy));
    }

    @PostMapping("/{policyNumber}/cancel")
    @Operation(summary = "Cancel a policy", description = "Cancels a policy with pro-rata refund")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Policy cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "404", description = "Policy not found")})
    public ResponseEntity<PolicyResponse> cancelPolicy(
            @Parameter(description = "Policy number") @PathVariable String policyNumber,
            @RequestBody CancelRequest request) {
        Policy policy = policyLifecycleService.cancelPolicy(policyNumber, request.reason(),
                request.cancellationCode());
        return ResponseEntity.ok(mapToResponse(policy));
    }

    @PostMapping("/{policyNumber}/suspend")
    @Operation(summary = "Suspend a policy",
            description = "Suspends a policy (e.g., for non-payment)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Policy suspended successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "404", description = "Policy not found")})
    public ResponseEntity<PolicyResponse> suspendPolicy(
            @Parameter(description = "Policy number") @PathVariable String policyNumber,
            @RequestBody SuspendRequest request) {
        Policy policy = policyLifecycleService.suspendPolicy(policyNumber, request.reason());
        return ResponseEntity.ok(mapToResponse(policy));
    }

    @PostMapping("/{policyNumber}/reactivate")
    @Operation(summary = "Reactivate a policy", description = "Reactivates a suspended policy")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Policy reactivated successfully"),
            @ApiResponse(responseCode = "400", description = "Policy is not suspended"),
            @ApiResponse(responseCode = "404", description = "Policy not found")})
    public ResponseEntity<PolicyResponse> reactivatePolicy(
            @Parameter(description = "Policy number") @PathVariable String policyNumber) {
        Policy policy = policyLifecycleService.reactivatePolicy(policyNumber);
        return ResponseEntity.ok(mapToResponse(policy));
    }

    @PostMapping("/{policyNumber}/expire")
    @Operation(summary = "Expire a policy", description = "Marks a policy as expired")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Policy expired successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "404", description = "Policy not found")})
    public ResponseEntity<PolicyResponse> expirePolicy(
            @Parameter(description = "Policy number") @PathVariable String policyNumber) {
        Policy policy = policyLifecycleService.expirePolicy(policyNumber);
        return ResponseEntity.ok(mapToResponse(policy));
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

    public record IssueRequest(String documentUrl) {
    }
    public record CancelRequest(String reason, String cancellationCode) {
    }
    public record SuspendRequest(String reason) {
    }
}
