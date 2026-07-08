package com.enterprise.insurance.core.controller.claim;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.enterprise.insurance.core.domain.claim.AdjudicationResult;
import com.enterprise.insurance.core.domain.claim.Claim;
import com.enterprise.insurance.core.domain.claim.ClaimHistory;
import com.enterprise.insurance.core.dto.claim.ApprovalRequest;
import com.enterprise.insurance.core.dto.claim.ClaimResponse;
import com.enterprise.insurance.core.dto.claim.RejectionRequest;
import com.enterprise.insurance.core.service.claim.ClaimAdjudicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/claims/{claimNumber}/adjudication")
@Tag(name = "Claim Adjudication", description = "Claim evaluation, approval, and rejection API")
public class ClaimAdjudicationController {

    private final ClaimAdjudicationService adjudicationService;

    public ClaimAdjudicationController(ClaimAdjudicationService adjudicationService) {
        this.adjudicationService = adjudicationService;
    }

    @PostMapping("/evaluate")
    @Operation(summary = "Evaluate claim",
            description = "Evaluates coverage and calculates settlement amount")
    public ResponseEntity<AdjudicationResult> evaluateClaim(@PathVariable String claimNumber) {
        AdjudicationResult result = adjudicationService.evaluateClaim(claimNumber);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/approve")
    @Operation(summary = "Approve claim",
            description = "Approves a claim with the specified amount")
    public ResponseEntity<ClaimResponse> approveClaim(@PathVariable String claimNumber,
            @Valid @RequestBody ApprovalRequest request) {
        Claim claim = adjudicationService.approveClaim(claimNumber, request);
        return ResponseEntity.ok(toResponse(claim));
    }

    @PostMapping("/partial-approve")
    @Operation(summary = "Partially approve claim",
            description = "Partially approves a claim with reduced amount")
    public ResponseEntity<ClaimResponse> partialApproveClaim(@PathVariable String claimNumber,
            @Valid @RequestBody ApprovalRequest request) {
        Claim claim = adjudicationService.partialApproveClaim(claimNumber, request);
        return ResponseEntity.ok(toResponse(claim));
    }

    @PostMapping("/reject")
    @Operation(summary = "Reject claim", description = "Rejects a claim with reason")
    public ResponseEntity<ClaimResponse> rejectClaim(@PathVariable String claimNumber,
            @Valid @RequestBody RejectionRequest request) {
        Claim claim = adjudicationService.rejectClaim(claimNumber, request);
        return ResponseEntity.ok(toResponse(claim));
    }

    @PostMapping("/fraud-review")
    @Operation(summary = "Require fraud review", description = "Flags a claim for fraud review")
    public ResponseEntity<ClaimResponse> requireFraudReview(@PathVariable String claimNumber,
            @RequestParam List<String> indicators, @RequestParam String performedBy) {
        Claim claim = adjudicationService.requireFraudReview(claimNumber, indicators, performedBy);
        return ResponseEntity.ok(toResponse(claim));
    }

    @GetMapping("/history")
    @Operation(summary = "Get adjudication history",
            description = "Retrieves the adjudication history for a claim")
    public ResponseEntity<List<ClaimHistory>> getAdjudicationHistory(
            @PathVariable String claimNumber) {
        List<ClaimHistory> history = adjudicationService.getAdjudicationHistory(claimNumber);
        return ResponseEntity.ok(history);
    }

    private ClaimResponse toResponse(Claim claim) {
        return ClaimResponse.builder().claimNumber(claim.getClaimNumber())
                .policyNumber(claim.getPolicyNumber()).customerId(claim.getCustomerId().toString())
                .customerName(claim.getCustomerName()).status(claim.getStatus().name())
                .claimType(claim.getClaimType().name()).lineOfBusiness(claim.getLineOfBusiness())
                .incidentDate(claim.getIncidentDate()).incidentLocation(claim.getIncidentLocation())
                .claimedAmount(claim.getClaimedAmount()).approvedAmount(claim.getApprovedAmount())
                .paidAmount(claim.getPaidAmount()).fraudScore(claim.getFraudScore())
                .handlerName(claim.getHandlerName()).adjusterName(claim.getAdjusterName())
                .reportedDate(claim.getReportedDate()).closedAt(claim.getClosedAt())
                .settlementDays(claim.getSettlementDays()).details(claim.getDynamicAttributes())
                .createdAt(claim.getCreatedAt()).updatedAt(claim.getUpdatedAt()).build();
    }
}
