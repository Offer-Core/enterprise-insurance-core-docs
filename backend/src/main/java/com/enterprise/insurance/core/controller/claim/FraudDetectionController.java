package com.enterprise.insurance.core.controller.claim;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.enterprise.insurance.core.domain.claim.Claim;
import com.enterprise.insurance.core.domain.claim.FraudAssessment;
import com.enterprise.insurance.core.domain.claim.FraudRule;
import com.enterprise.insurance.core.dto.claim.ClaimResponse;
import com.enterprise.insurance.core.service.claim.FraudDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/claims/{claimNumber}/fraud")
@Tag(name = "Fraud Detection", description = "Fraud detection and investigation API")
public class FraudDetectionController {

    private final FraudDetectionService fraudDetectionService;

    public FraudDetectionController(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    @PostMapping("/evaluate")
    @Operation(summary = "Evaluate fraud risk",
            description = "Runs fraud detection rules and returns risk assessment")
    public ResponseEntity<FraudAssessment> evaluateFraud(@PathVariable String claimNumber) {
        FraudAssessment assessment = fraudDetectionService.evaluateFraud(claimNumber);
        return ResponseEntity.ok(assessment);
    }

    @GetMapping("/score")
    @Operation(summary = "Get fraud score",
            description = "Returns the current fraud score for a claim")
    public ResponseEntity<BigDecimal> getFraudScore(@PathVariable String claimNumber) {
        BigDecimal score = fraudDetectionService.getFraudScore(claimNumber);
        return ResponseEntity.ok(score);
    }

    @GetMapping("/indicators")
    @Operation(summary = "Get fraud indicators",
            description = "Returns the fraud indicators triggered for a claim")
    public ResponseEntity<List<String>> getFraudIndicators(@PathVariable String claimNumber) {
        List<String> indicators = fraudDetectionService.getFraudIndicators(claimNumber);
        return ResponseEntity.ok(indicators);
    }

    @PostMapping("/manual-review")
    @Operation(summary = "Start manual fraud review",
            description = "Initiates a manual fraud review process")
    public ResponseEntity<ClaimResponse> manualFraudReview(@PathVariable String claimNumber,
            @RequestParam String reviewedBy) {
        Claim claim = fraudDetectionService.manualFraudReview(claimNumber, reviewedBy);
        return ResponseEntity.ok(toResponse(claim));
    }

    @PostMapping("/confirm")
    @Operation(summary = "Confirm fraud", description = "Confirms that a claim is fraudulent")
    public ResponseEntity<ClaimResponse> confirmFraud(@PathVariable String claimNumber,
            @RequestParam String confirmedBy) {
        Claim claim = fraudDetectionService.confirmFraud(claimNumber, confirmedBy);
        return ResponseEntity.ok(toResponse(claim));
    }

    @PostMapping("/clear")
    @Operation(summary = "Clear fraud",
            description = "Clears fraud suspicion and returns claim to adjudication")
    public ResponseEntity<ClaimResponse> clearFraud(@PathVariable String claimNumber,
            @RequestParam String clearedBy) {
        Claim claim = fraudDetectionService.clearFraud(claimNumber, clearedBy);
        return ResponseEntity.ok(toResponse(claim));
    }

    @GetMapping("/suspicious")
    @Operation(summary = "Get suspicious claims",
            description = "Returns all claims flagged for fraud review")
    public ResponseEntity<List<ClaimResponse>> getSuspiciousClaims() {
        List<Claim> claims = fraudDetectionService.getSuspiciousClaims();
        return ResponseEntity.ok(claims.stream().map(this::toResponse).toList());
    }

    @PutMapping("/rules")
    @Operation(summary = "Update fraud rules",
            description = "Creates or updates a fraud detection rule")
    public ResponseEntity<FraudRule> updateFraudRules(@RequestBody FraudRule rule) {
        FraudRule saved = fraudDetectionService.updateFraudRules(rule);
        return ResponseEntity.ok(saved);
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
