package com.enterprise.insurance.core.api.policy;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.enterprise.insurance.core.domain.endorsement.Endorsement;
import com.enterprise.insurance.core.dto.endorsement.EndorsementRequest;
import com.enterprise.insurance.core.dto.endorsement.EndorsementResponse;
import com.enterprise.insurance.core.service.endorsement.EndorsementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/endorsements")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Endorsements", description = "Policy endorsement management APIs")
public class EndorsementController {

    private final EndorsementService endorsementService;

    @PostMapping
    @Operation(summary = "Create an endorsement request")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Endorsement created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Policy not found")})
    public ResponseEntity<EndorsementResponse> createEndorsement(
            @Valid @RequestBody EndorsementRequest request, @RequestParam UUID userId,
            @RequestParam(defaultValue = "default") String tenantId) {
        Endorsement endorsement = endorsementService.createEndorsement(request, userId, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(endorsement));
    }

    @PostMapping("/{endorsementNumber}/approve")
    @Operation(summary = "Approve an endorsement request")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Endorsement approved"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "404", description = "Endorsement not found")})
    public ResponseEntity<EndorsementResponse> approveEndorsement(
            @Parameter(description = "Endorsement number") @PathVariable String endorsementNumber,
            @RequestParam UUID approvedBy) {
        Endorsement endorsement =
                endorsementService.approveEndorsement(endorsementNumber, approvedBy);
        return ResponseEntity.ok(mapToResponse(endorsement));
    }

    @PostMapping("/{endorsementNumber}/reject")
    @Operation(summary = "Reject an endorsement request")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Endorsement rejected"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "404", description = "Endorsement not found")})
    public ResponseEntity<EndorsementResponse> rejectEndorsement(
            @Parameter(description = "Endorsement number") @PathVariable String endorsementNumber,
            @RequestParam UUID rejectedBy, @RequestBody RejectRequest request) {
        Endorsement endorsement = endorsementService.rejectEndorsement(endorsementNumber,
                rejectedBy, request.reason());
        return ResponseEntity.ok(mapToResponse(endorsement));
    }

    @PostMapping("/{endorsementNumber}/apply")
    @Operation(summary = "Apply an approved endorsement to the policy")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Endorsement applied"),
            @ApiResponse(responseCode = "400", description = "Not approved yet"),
            @ApiResponse(responseCode = "404", description = "Endorsement not found")})
    public ResponseEntity<EndorsementResponse> applyEndorsement(
            @Parameter(description = "Endorsement number") @PathVariable String endorsementNumber) {
        Endorsement endorsement = endorsementService.applyEndorsement(endorsementNumber);
        return ResponseEntity.ok(mapToResponse(endorsement));
    }

    @GetMapping("/{endorsementNumber}")
    @Operation(summary = "Get endorsement by number")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Endorsement found"),
            @ApiResponse(responseCode = "404", description = "Endorsement not found")})
    public ResponseEntity<EndorsementResponse> getEndorsement(
            @Parameter(description = "Endorsement number") @PathVariable String endorsementNumber) {
        Endorsement endorsement = endorsementService.findEndorsementByNumber(endorsementNumber);
        return ResponseEntity.ok(mapToResponse(endorsement));
    }

    @GetMapping("/by-policy/{policyId}")
    @Operation(summary = "Get all endorsements for a policy")
    public ResponseEntity<List<EndorsementResponse>> getEndorsementsByPolicy(
            @Parameter(description = "Policy UUID") @PathVariable UUID policyId) {
        List<Endorsement> endorsements = endorsementService.getEndorsementsByPolicy(policyId);
        return ResponseEntity.ok(endorsements.stream().map(this::mapToResponse).toList());
    }

    private EndorsementResponse mapToResponse(Endorsement endorsement) {
        return EndorsementResponse.builder().endorsementNumber(endorsement.getEndorsementNumber())
                .endorsementType(endorsement.getEndorsementType().name())
                .description(endorsement.getDescription())
                .effectiveDate(endorsement.getEffectiveDate())
                .premiumAdjustment(endorsement.getPremiumAdjustment())
                .status(endorsement.getStatus().name()).createdAt(endorsement.getCreatedAt())
                .approvedAt(endorsement.getApprovedAt())
                .approvedBy(
                        endorsement.getApprovedBy() != null ? endorsement.getApprovedBy().toString()
                                : null)
                .build();
    }

    public record RejectRequest(String reason) {
    }
}
