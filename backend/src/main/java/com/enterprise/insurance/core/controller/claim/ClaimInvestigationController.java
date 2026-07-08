package com.enterprise.insurance.core.controller.claim;

import java.util.UUID;
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
import com.enterprise.insurance.core.domain.claim.ClaimStatus;
import com.enterprise.insurance.core.dto.claim.ClaimResponse;
import com.enterprise.insurance.core.dto.claim.EvidenceRequest;
import com.enterprise.insurance.core.service.claim.ClaimInvestigationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/claims/{claimNumber}/investigation")
@Tag(name = "Claim Investigation", description = "Claim investigation and evidence management API")
public class ClaimInvestigationController {

    private final ClaimInvestigationService investigationService;

    public ClaimInvestigationController(ClaimInvestigationService investigationService) {
        this.investigationService = investigationService;
    }

    @PostMapping("/start")
    @Operation(summary = "Start investigation",
            description = "Initiates the investigation process for a claim")
    public ResponseEntity<ClaimResponse> startInvestigation(@PathVariable String claimNumber,
            @RequestParam String performedBy) {
        Claim claim = investigationService.startInvestigation(claimNumber, performedBy);
        return ResponseEntity.ok(toResponse(claim));
    }

    @PostMapping("/assign-adjuster")
    @Operation(summary = "Assign adjuster",
            description = "Assigns an adjuster to investigate the claim")
    public ResponseEntity<ClaimResponse> assignAdjuster(@PathVariable String claimNumber,
            @RequestParam UUID adjusterId, @RequestParam String adjusterName,
            @RequestParam String performedBy) {
        Claim claim = investigationService.assignAdjuster(claimNumber, adjusterId, adjusterName,
                performedBy);
        return ResponseEntity.ok(toResponse(claim));
    }

    @PostMapping("/evidence")
    @Operation(summary = "Add evidence", description = "Adds evidence to a claim investigation")
    public ResponseEntity<ClaimResponse> addEvidence(@PathVariable String claimNumber,
            @Valid @RequestBody EvidenceRequest request) {
        Claim claim = investigationService.addEvidence(claimNumber, request);
        return ResponseEntity.ok(toResponse(claim));
    }

    @PostMapping("/police-report")
    @Operation(summary = "Add police report",
            description = "Adds a police report number to the claim")
    public ResponseEntity<ClaimResponse> addPoliceReport(@PathVariable String claimNumber,
            @RequestParam String policeReportNumber, @RequestParam String performedBy) {
        Claim claim =
                investigationService.addPoliceReport(claimNumber, policeReportNumber, performedBy);
        return ResponseEntity.ok(toResponse(claim));
    }

    @PostMapping("/assess-damage")
    @Operation(summary = "Assess damage", description = "Records damage assessment for the claim")
    public ResponseEntity<ClaimResponse> assessDamage(@PathVariable String claimNumber,
            @RequestParam String damageAssessment, @RequestParam String performedBy) {
        Claim claim = investigationService.assessDamage(claimNumber, damageAssessment, performedBy);
        return ResponseEntity.ok(toResponse(claim));
    }

    @PutMapping("/status")
    @Operation(summary = "Update investigation status",
            description = "Updates the investigation status")
    public ResponseEntity<ClaimResponse> updateInvestigationStatus(@PathVariable String claimNumber,
            @RequestParam ClaimStatus newStatus, @RequestParam String performedBy) {
        Claim claim =
                investigationService.updateInvestigationStatus(claimNumber, newStatus, performedBy);
        return ResponseEntity.ok(toResponse(claim));
    }

    @GetMapping("/status")
    @Operation(summary = "Get investigation status",
            description = "Gets the current investigation status")
    public ResponseEntity<String> getInvestigationStatus(@PathVariable String claimNumber) {
        String status = investigationService.getInvestigationStatus(claimNumber);
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
