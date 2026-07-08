package com.enterprise.insurance.core.controller.claim;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.enterprise.insurance.core.domain.claim.Claim;
import com.enterprise.insurance.core.domain.claim.ClaimHistory;
import com.enterprise.insurance.core.domain.claim.ClaimWorkflowDefinition;
import com.enterprise.insurance.core.dto.claim.ClaimResponse;
import com.enterprise.insurance.core.service.claim.ClaimWorkflowService;
import com.enterprise.insurance.core.service.claim.ClaimWorkflowService.SLAStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/claims/{claimNumber}/workflow")
@Tag(name = "Claim Workflow", description = "Claim workflow and SLA management API")
public class ClaimWorkflowController {

    private final ClaimWorkflowService workflowService;

    public ClaimWorkflowController(ClaimWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    @Operation(summary = "Get workflow definition",
            description = "Retrieves the workflow definition for a claim")
    public ResponseEntity<ClaimWorkflowDefinition> getWorkflow(@PathVariable String claimNumber) {
        ClaimWorkflowDefinition workflow = workflowService.getWorkflow(claimNumber);
        return ResponseEntity.ok(workflow);
    }

    @PostMapping("/transition")
    @Operation(summary = "Transition state",
            description = "Transitions a claim to a new state via an action")
    public ResponseEntity<ClaimResponse> transitionState(@PathVariable String claimNumber,
            @RequestParam String action, @RequestParam String performedBy) {
        Claim claim = workflowService.transitionState(claimNumber, action, performedBy);
        return ResponseEntity.ok(toResponse(claim));
    }

    @GetMapping("/actions")
    @Operation(summary = "Get available actions",
            description = "Returns the list of available actions for the current state")
    public ResponseEntity<List<String>> getAvailableActions(@PathVariable String claimNumber) {
        List<String> actions = workflowService.getAvailableActions(claimNumber);
        return ResponseEntity.ok(actions);
    }

    @GetMapping("/history")
    @Operation(summary = "Get workflow history",
            description = "Retrieves the full workflow history for a claim")
    public ResponseEntity<List<ClaimHistory>> getWorkflowHistory(@PathVariable String claimNumber) {
        List<ClaimHistory> history = workflowService.getWorkflowHistory(claimNumber);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/sla")
    @Operation(summary = "Get SLA status",
            description = "Returns the SLA status for the current workflow state")
    public ResponseEntity<SLAStatus> getSLAStatus(@PathVariable String claimNumber) {
        SLAStatus slaStatus = workflowService.getSLAStatus(claimNumber);
        return ResponseEntity.ok(slaStatus);
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
