package com.enterprise.insurance.core.controller.claim;

import java.math.BigDecimal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.enterprise.insurance.core.domain.claim.Claim;
import com.enterprise.insurance.core.dto.claim.ClaimResponse;
import com.enterprise.insurance.core.dto.claim.PaymentRequest;
import com.enterprise.insurance.core.service.claim.ClaimSettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/claims/{claimNumber}/settlement")
@Tag(name = "Claim Settlement", description = "Claim payment and settlement API")
public class ClaimSettlementController {

    private final ClaimSettlementService settlementService;

    public ClaimSettlementController(ClaimSettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @PostMapping("/pay")
    @Operation(summary = "Process payment",
            description = "Processes a payment for an approved claim")
    public ResponseEntity<ClaimResponse> processPayment(@PathVariable String claimNumber,
            @Valid @RequestBody PaymentRequest request) {
        Claim claim = settlementService.processPayment(claimNumber, request);
        return ResponseEntity.ok(toResponse(claim));
    }

    @GetMapping("/calculate")
    @Operation(summary = "Calculate payment amount",
            description = "Calculates the outstanding payment amount")
    public ResponseEntity<BigDecimal> calculatePayment(@PathVariable String claimNumber) {
        BigDecimal amount = settlementService.calculatePaymentAmount(claimNumber);
        return ResponseEntity.ok(amount);
    }

    @PostMapping("/close")
    @Operation(summary = "Close claim", description = "Closes a claim after settlement")
    public ResponseEntity<ClaimResponse> closeClaim(@PathVariable String claimNumber,
            @RequestParam String closedBy) {
        Claim claim = settlementService.closeClaim(claimNumber, closedBy);
        return ResponseEntity.ok(toResponse(claim));
    }

    @PostMapping("/reopen")
    @Operation(summary = "Reopen claim", description = "Reopens a closed claim")
    public ResponseEntity<ClaimResponse> reopenClaim(@PathVariable String claimNumber,
            @RequestParam String reason, @RequestParam String reopenedBy) {
        Claim claim = settlementService.reopenClaim(claimNumber, reason, reopenedBy);
        return ResponseEntity.ok(toResponse(claim));
    }

    @GetMapping("/payment-status")
    @Operation(summary = "Get payment status",
            description = "Gets the current payment status of a claim")
    public ResponseEntity<String> getPaymentStatus(@PathVariable String claimNumber) {
        String status = settlementService.getClaimPaymentStatus(claimNumber);
        return ResponseEntity.ok(status);
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
