package com.enterprise.insurance.core.controller.claim;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
import com.enterprise.insurance.core.dto.claim.ClaimRegistrationRequest;
import com.enterprise.insurance.core.dto.claim.ClaimResponse;
import com.enterprise.insurance.core.dto.claim.ClaimSearchRequest;
import com.enterprise.insurance.core.dto.claim.ClaimTimelineEntry;
import com.enterprise.insurance.core.repository.claim.ClaimRepository;
import com.enterprise.insurance.core.service.claim.ClaimRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/claims")
@Tag(name = "Claims", description = "Claim management API for the insurance platform")
public class ClaimController {

    private final ClaimRegistrationService claimRegistrationService;
    private final ClaimRepository claimRepository;

    public ClaimController(ClaimRegistrationService claimRegistrationService,
            ClaimRepository claimRepository) {
        this.claimRegistrationService = claimRegistrationService;
        this.claimRepository = claimRepository;
    }

    @PostMapping
    @Operation(summary = "Register a new claim",
            description = "Creates a new claim with motor details and evidence")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Claim registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid claim data"),
            @ApiResponse(responseCode = "404", description = "Policy or customer not found")})
    public ResponseEntity<ClaimResponse> registerClaim(
            @Valid @RequestBody ClaimRegistrationRequest request) {
        Claim claim = claimRegistrationService.registerClaim(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(claim));
    }

    @GetMapping("/{claimNumber}")
    @Operation(summary = "Get claim by claim number",
            description = "Retrieves a claim by its unique claim number")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Claim found"),
            @ApiResponse(responseCode = "404", description = "Claim not found")})
    public ResponseEntity<ClaimResponse> getClaim(
            @Parameter(description = "Claim number") @PathVariable String claimNumber) {
        Claim claim = claimRegistrationService.getClaimByNumber(claimNumber);
        return ResponseEntity.ok(toResponse(claim));
    }

    @GetMapping("/policy/{policyNumber}")
    @Operation(summary = "Get claims by policy number",
            description = "Retrieves all claims associated with a policy")
    public ResponseEntity<List<ClaimResponse>> getClaimsByPolicy(
            @Parameter(description = "Policy number") @PathVariable String policyNumber) {
        List<Claim> claims = claimRegistrationService.getClaimsByPolicy(policyNumber);
        return ResponseEntity.ok(claims.stream().map(this::toResponse).toList());
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get claims by customer",
            description = "Retrieves all claims for a customer")
    public ResponseEntity<List<ClaimResponse>> getClaimsByCustomer(
            @Parameter(description = "Customer UUID") @PathVariable String customerId) {
        List<Claim> claims = claimRegistrationService.getClaimsByCustomer(customerId);
        return ResponseEntity.ok(claims.stream().map(this::toResponse).toList());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get claims by status",
            description = "Retrieves all claims with a specific status")
    public ResponseEntity<List<ClaimResponse>> getClaimsByStatus(
            @Parameter(description = "Claim status") @PathVariable ClaimStatus status) {
        List<Claim> claims = claimRegistrationService.getClaimsByStatus(status);
        return ResponseEntity.ok(claims.stream().map(this::toResponse).toList());
    }

    @GetMapping("/open")
    @Operation(summary = "Get open claims",
            description = "Retrieves all claims that are not closed or rejected")
    public ResponseEntity<List<ClaimResponse>> getOpenClaims() {
        List<Claim> claims = claimRegistrationService.getOpenClaims();
        return ResponseEntity.ok(claims.stream().map(this::toResponse).toList());
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get claims by date range",
            description = "Retrieves claims within a date range")
    public ResponseEntity<List<ClaimResponse>> getClaimsByDateRange(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<Claim> claims = claimRegistrationService.getClaimsByDateRange(from, to);
        return ResponseEntity.ok(claims.stream().map(this::toResponse).toList());
    }

    @GetMapping("/line-of-business/{lob}")
    @Operation(summary = "Get claims by line of business",
            description = "Retrieves claims for a specific line of business")
    public ResponseEntity<List<ClaimResponse>> getClaimsByLineOfBusiness(
            @Parameter(description = "Line of business code") @PathVariable String lob) {
        List<Claim> claims = claimRegistrationService.getClaimsByLineOfBusiness(lob);
        return ResponseEntity.ok(claims.stream().map(this::toResponse).toList());
    }

    @GetMapping("/search")
    @Operation(summary = "Search claims", description = "Advanced search with multiple filters")
    public ResponseEntity<Page<ClaimResponse>> searchClaims(@Valid ClaimSearchRequest searchRequest,
            Pageable pageable) {
        Page<Claim> claims = claimRepository.searchClaims(searchRequest.getClaimNumber(),
                searchRequest.getPolicyNumber(),
                searchRequest.getCustomerId() != null
                        ? java.util.UUID.fromString(searchRequest.getCustomerId())
                        : null,
                searchRequest.getStatus(), searchRequest.getClaimType(),
                searchRequest.getLineOfBusiness(),
                searchRequest.getHandlerId() != null
                        ? java.util.UUID.fromString(searchRequest.getHandlerId())
                        : null,
                searchRequest.getFraudReviewRequired(), searchRequest.getTenantId(), pageable);
        return ResponseEntity.ok(claims.map(this::toResponse));
    }

    @PutMapping("/{claimNumber}/assign-handler")
    @Operation(summary = "Assign claim handler",
            description = "Assigns a handler to manage the claim")
    public ResponseEntity<ClaimResponse> assignHandler(@PathVariable String claimNumber,
            @RequestParam java.util.UUID handlerId, @RequestParam String handlerName,
            @RequestParam String assignedBy) {
        Claim claim = claimRegistrationService.assignHandler(claimNumber, handlerId, handlerName,
                assignedBy);
        return ResponseEntity.ok(toResponse(claim));
    }

    @GetMapping("/{claimNumber}/timeline")
    @Operation(summary = "Get claim timeline",
            description = "Retrieves the full event timeline for a claim")
    public ResponseEntity<List<ClaimTimelineEntry>> getClaimTimeline(
            @PathVariable String claimNumber) {
        List<ClaimTimelineEntry> timeline = claimRegistrationService.getClaimTimeline(claimNumber);
        return ResponseEntity.ok(timeline);
    }

    @GetMapping("/counts")
    @Operation(summary = "Get claim counts", description = "Returns aggregate counts for dashboard")
    public ResponseEntity<ClaimCounts> getClaimCounts() {
        long open = claimRepository.countOpenClaims();
        long registered = claimRepository.countByStatus(ClaimStatus.REGISTERED);
        long investigating = claimRepository.countByStatus(ClaimStatus.INVESTIGATING);
        long approved = claimRepository.countByStatus(ClaimStatus.APPROVED);
        long paid = claimRepository.countByStatus(ClaimStatus.PAID);
        long closed = claimRepository.countByStatus(ClaimStatus.CLOSED);
        long fraudReview = claimRepository.countByFraudReviewRequiredTrue();

        return ResponseEntity.ok(new ClaimCounts(open, registered, investigating, approved, paid,
                closed, fraudReview));
    }

    public record ClaimCounts(long open, long registered, long investigating, long approved,
            long paid, long closed, long fraudReview) {
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
