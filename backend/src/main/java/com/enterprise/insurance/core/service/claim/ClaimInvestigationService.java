package com.enterprise.insurance.core.service.claim;

import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.enterprise.insurance.core.domain.claim.Claim;
import com.enterprise.insurance.core.domain.claim.ClaimHistory;
import com.enterprise.insurance.core.domain.claim.ClaimStatus;
import com.enterprise.insurance.core.dto.claim.EvidenceRequest;
import com.enterprise.insurance.core.exception.EntityNotFoundException;
import com.enterprise.insurance.core.repository.claim.ClaimHistoryRepository;
import com.enterprise.insurance.core.repository.claim.ClaimRepository;
import com.enterprise.insurance.core.repository.claim.MotorClaimDetailsRepository;

@Service
@Transactional
public class ClaimInvestigationService {

    private static final Logger log = LoggerFactory.getLogger(ClaimInvestigationService.class);

    private final ClaimRepository claimRepository;
    private final ClaimHistoryRepository claimHistoryRepository;
    private final MotorClaimDetailsRepository motorClaimDetailsRepository;
    private final ClaimEventPublisher eventPublisher;

    public ClaimInvestigationService(ClaimRepository claimRepository,
            ClaimHistoryRepository claimHistoryRepository,
            MotorClaimDetailsRepository motorClaimDetailsRepository,
            ClaimEventPublisher eventPublisher) {
        this.claimRepository = claimRepository;
        this.claimHistoryRepository = claimHistoryRepository;
        this.motorClaimDetailsRepository = motorClaimDetailsRepository;
        this.eventPublisher = eventPublisher;
    }

    public Claim startInvestigation(String claimNumber, String performedBy) {
        Claim claim = getClaim(claimNumber);
        String previousStatus = claim.getStatus().name();
        claim.transitionTo(ClaimStatus.INVESTIGATING);
        claim = claimRepository.save(claim);

        saveHistory(claim, "INVESTIGATION_STARTED", previousStatus, claim.getStatus().name(),
                Map.of("description", "Investigation started"), performedBy);

        eventPublisher.publishClaimInvestigationStarted(claim, performedBy);
        eventPublisher.publishClaimStatusChanged(claim, previousStatus, "Investigation started",
                performedBy);

        log.info("Investigation started for claim: {}", claimNumber);
        return claim;
    }

    public Claim assignAdjuster(String claimNumber, UUID adjusterId, String adjusterName,
            String performedBy) {
        Claim claim = getClaim(claimNumber);
        claim.setAdjusterId(adjusterId);
        claim.setAdjusterName(adjusterName);
        claim = claimRepository.save(claim);

        saveHistory(claim, "ADJUSTER_ASSIGNED", claim.getStatus().name(), claim.getStatus().name(),
                Map.of("adjusterId", adjusterId.toString(), "adjusterName", adjusterName),
                performedBy);

        log.info("Adjuster {} assigned to claim: {}", adjusterName, claimNumber);
        return claim;
    }

    public Claim addEvidence(String claimNumber, EvidenceRequest request) {
        Claim claim = getClaim(claimNumber);

        saveHistory(claim, "EVIDENCE_ADDED", claim.getStatus().name(), claim.getStatus().name(),
                Map.of("evidenceType", request.getEvidenceType(), "description",
                        request.getDescription(), "documentUrl", request.getDocumentUrl(), "notes",
                        request.getNotes()),
                request.getUploadedBy());

        log.info("Evidence added to claim: {} (type: {})", claimNumber, request.getEvidenceType());
        return claim;
    }

    public Claim addPoliceReport(String claimNumber, String policeReportNumber,
            String performedBy) {
        Claim claim = getClaim(claimNumber);

        // Update motor claim details
        motorClaimDetailsRepository.findByClaimId(claim.getId()).ifPresent(motorDetails -> {
            motorDetails.setPoliceReportNumber(policeReportNumber);
            motorClaimDetailsRepository.save(motorDetails);
        });

        saveHistory(claim, "POLICE_REPORT_ADDED", claim.getStatus().name(),
                claim.getStatus().name(), Map.of("policeReportNumber", policeReportNumber),
                performedBy);

        log.info("Police report {} added to claim: {}", policeReportNumber, claimNumber);
        return claim;
    }

    public Claim assessDamage(String claimNumber, String damageAssessment, String performedBy) {
        Claim claim = getClaim(claimNumber);

        motorClaimDetailsRepository.findByClaimId(claim.getId()).ifPresent(motorDetails -> {
            motorDetails.setDamageAssessment(damageAssessment);
            motorClaimDetailsRepository.save(motorDetails);
        });

        saveHistory(claim, "DAMAGE_ASSESSED", claim.getStatus().name(), claim.getStatus().name(),
                Map.of("damageAssessment", damageAssessment), performedBy);

        log.info("Damage assessed for claim: {}", claimNumber);
        return claim;
    }

    public Claim updateInvestigationStatus(String claimNumber, ClaimStatus newStatus,
            String performedBy) {
        Claim claim = getClaim(claimNumber);
        String previousStatus = claim.getStatus().name();
        claim.transitionTo(newStatus);
        claim = claimRepository.save(claim);

        saveHistory(claim, "INVESTIGATION_STATUS_UPDATED", previousStatus, newStatus.name(),
                Map.of("description", "Investigation status updated"), performedBy);

        eventPublisher.publishClaimStatusChanged(claim, previousStatus,
                "Investigation status updated", performedBy);

        log.info("Investigation status updated for claim: {} ({} -> {})", claimNumber,
                previousStatus, newStatus);
        return claim;
    }

    public String getInvestigationStatus(String claimNumber) {
        Claim claim = getClaim(claimNumber);
        return claim.getStatus().name();
    }

    private Claim getClaim(String claimNumber) {
        return claimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found: " + claimNumber));
    }

    private void saveHistory(Claim claim, String eventType, String previousStatus, String newStatus,
            Map<String, Object> eventData, String performedBy) {
        ClaimHistory history = ClaimHistory.builder().claimId(claim.getId()).eventType(eventType)
                .previousStatus(previousStatus).newStatus(newStatus).eventData(eventData)
                .performedBy(UUID.fromString(performedBy)).tenantId(claim.getTenantId()).build();
        claimHistoryRepository.save(history);
    }
}
