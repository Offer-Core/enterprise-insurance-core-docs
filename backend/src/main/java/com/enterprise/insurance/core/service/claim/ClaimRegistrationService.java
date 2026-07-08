package com.enterprise.insurance.core.service.claim;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.enterprise.insurance.core.domain.claim.Claim;
import com.enterprise.insurance.core.domain.claim.ClaimHistory;
import com.enterprise.insurance.core.domain.claim.ClaimStatus;
import com.enterprise.insurance.core.domain.claim.ClaimType;
import com.enterprise.insurance.core.domain.claim.MotorClaimDetails;
import com.enterprise.insurance.core.domain.claim.VehicleDamage;
import com.enterprise.insurance.core.dto.claim.ClaimRegistrationRequest;
import com.enterprise.insurance.core.dto.claim.ClaimTimelineEntry;
import com.enterprise.insurance.core.exception.EntityNotFoundException;
import com.enterprise.insurance.core.repository.claim.ClaimHistoryRepository;
import com.enterprise.insurance.core.repository.claim.ClaimRepository;
import com.enterprise.insurance.core.repository.claim.MotorClaimDetailsRepository;
import com.enterprise.insurance.core.repository.claim.VehicleDamageRepository;
import jakarta.validation.Valid;

@Service
@Transactional
public class ClaimRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(ClaimRegistrationService.class);

    private final ClaimRepository claimRepository;
    private final ClaimHistoryRepository claimHistoryRepository;
    private final MotorClaimDetailsRepository motorClaimDetailsRepository;
    private final VehicleDamageRepository vehicleDamageRepository;
    private final ClaimEventPublisher eventPublisher;

    public ClaimRegistrationService(ClaimRepository claimRepository,
            ClaimHistoryRepository claimHistoryRepository,
            MotorClaimDetailsRepository motorClaimDetailsRepository,
            VehicleDamageRepository vehicleDamageRepository, ClaimEventPublisher eventPublisher) {
        this.claimRepository = claimRepository;
        this.claimHistoryRepository = claimHistoryRepository;
        this.motorClaimDetailsRepository = motorClaimDetailsRepository;
        this.vehicleDamageRepository = vehicleDamageRepository;
        this.eventPublisher = eventPublisher;
    }

    public Claim registerClaim(@Valid ClaimRegistrationRequest request) {
        log.info("Registering claim for policy: {}", request.getPolicyNumber());

        String claimNumber =
                generateClaimNumber(request.getClaimType(), request.getLineOfBusiness());

        Claim claim =
                Claim.builder().claimNumber(claimNumber).policyNumber(request.getPolicyNumber())
                        .customerId(UUID.fromString(request.getCustomerId()))
                        .incidentDate(request.getIncidentDate())
                        .incidentTime(request.getIncidentTime() != null
                                ? java.time.LocalTime.parse(request.getIncidentTime())
                                : null)
                        .incidentLocation(request.getIncidentLocation())
                        .incidentLatitude(request.getIncidentLatitude())
                        .incidentLongitude(request.getIncidentLongitude())
                        .claimType(request.getClaimType()).claimedAmount(request.getClaimedAmount())
                        .currency(request.getCurrency()).lineOfBusiness("MOTOR")
                        .productCode("MOTOR_TPL").status(ClaimStatus.REGISTERED)
                        .reportedDate(LocalDateTime.now())
                        .dynamicAttributes(request.getAdditionalAttributes())
                        .tenantId(request.getTenantId())
                        .createdBy(UUID.fromString(request.getCreatedBy())).build();

        claim = claimRepository.save(claim);

        // Save motor claim details if provided
        if (request.getMotorDetails() != null) {
            saveMotorClaimDetails(claim, request.getMotorDetails());
        }

        // Save claim history
        saveClaimHistory(claim, "CLAIM_REGISTERED", null, ClaimStatus.REGISTERED.name(),
                Map.of("description", request.getDescription()), request.getCreatedBy());

        // Publish event
        eventPublisher.publishClaimRegistered(claim);

        log.info("Claim registered successfully: {}", claimNumber);
        return claim;
    }

    public Claim getClaimByNumber(String claimNumber) {
        return claimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found: " + claimNumber));
    }

    public List<Claim> getClaimsByPolicy(String policyNumber) {
        return claimRepository.findByPolicyNumber(policyNumber);
    }

    public List<Claim> getClaimsByCustomer(String customerId) {
        return claimRepository.findByCustomerId(UUID.fromString(customerId));
    }

    public List<Claim> getClaimsByStatus(ClaimStatus status) {
        return claimRepository.findByStatus(status);
    }

    public List<Claim> getClaimsByDateRange(LocalDate from, LocalDate to) {
        return claimRepository.findByIncidentDateBetween(from, to);
    }

    public List<Claim> getOpenClaims() {
        return claimRepository.findOpenClaims();
    }

    public List<Claim> getClaimsByLineOfBusiness(String lob) {
        return claimRepository.findByLineOfBusiness(lob);
    }

    public Claim assignHandler(String claimNumber, UUID handlerId, String handlerName,
            String assignedBy) {
        Claim claim = getClaimByNumber(claimNumber);
        claim.setHandlerId(handlerId);
        claim.setHandlerName(handlerName);
        claim = claimRepository.save(claim);

        saveClaimHistory(claim, "HANDLER_ASSIGNED", claim.getStatus().name(),
                claim.getStatus().name(),
                Map.of("handlerId", handlerId.toString(), "handlerName", handlerName), assignedBy);

        log.info("Handler {} assigned to claim: {}", handlerName, claimNumber);
        return claim;
    }

    public List<ClaimTimelineEntry> getClaimTimeline(String claimNumber) {
        Claim claim = getClaimByNumber(claimNumber);
        List<ClaimHistory> history =
                claimHistoryRepository.findByClaimIdOrderByOccurredAtAsc(claim.getId());

        return history.stream()
                .map(h -> ClaimTimelineEntry.builder().timestamp(h.getOccurredAt())
                        .eventType(h.getEventType())
                        .description((String) h.getEventData().get("description"))
                        .previousStatus(h.getPreviousStatus()).newStatus(h.getNewStatus())
                        .performedBy(h.getPerformedBy().toString()).details(h.getEventData())
                        .build())
                .toList();
    }

    private String generateClaimNumber(ClaimType claimType, String lineOfBusiness) {
        String lob = lineOfBusiness != null ? lineOfBusiness.substring(0, 4).toUpperCase() : "GEN";
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        String seq = String.format("%06d", (int) (System.currentTimeMillis() % 1000000));
        return String.format("CLM-%s-%s-%s", lob, year, seq);
    }

    private void saveMotorClaimDetails(Claim claim,
            ClaimRegistrationRequest.MotorClaimDetails motorReq) {
        MotorClaimDetails motorDetails = MotorClaimDetails.builder().claimId(claim.getId())
                .accidentType(motorReq.getAccidentType()).accidentCause(motorReq.getAccidentCause())
                .lossLocation(motorReq.getLossLocation())
                .policeReportNumber(motorReq.getPoliceReportNumber())
                .policeReportDate(motorReq.getPoliceReportDate()).atFault(motorReq.getAtFault())
                .faultPercentage(motorReq.getFaultPercentage())
                .thirdPartyNationalId(motorReq.getThirdPartyNationalId())
                .thirdPartyVehicle(motorReq.getThirdPartyVehicle())
                .thirdPartyInsuranceCompany(motorReq.getThirdPartyInsuranceCompany())
                .repairShopId(motorReq.getRepairShopId())
                .repairCostEstimate(motorReq.getRepairCostEstimate())
                .towTruckRequired(motorReq.getTowTruckRequired())
                .dynamicAttributes(motorReq.getAdditionalAttributes()).build();
        motorClaimDetailsRepository.save(motorDetails);

        // Save vehicle damages
        if (motorReq.getVehicleDamages() != null) {
            motorReq.getVehicleDamages().forEach(damageReq -> {
                VehicleDamage damage = VehicleDamage.builder().claimId(claim.getId())
                        .vehicleComponent(damageReq.getVehicleComponent())
                        .damageType(damageReq.getDamageType())
                        .damageSeverity(damageReq.getDamageSeverity())
                        .repairCost(damageReq.getRepairCost())
                        .replacementCost(damageReq.getReplacementCost())
                        .isReplacementRequired(damageReq.getIsReplacementRequired())
                        .notes(damageReq.getNotes()).build();
                vehicleDamageRepository.save(damage);
            });
        }
    }

    private void saveClaimHistory(Claim claim, String eventType, String previousStatus,
            String newStatus, Map<String, Object> eventData, String performedBy) {
        ClaimHistory history = ClaimHistory.builder().claimId(claim.getId()).eventType(eventType)
                .previousStatus(previousStatus).newStatus(newStatus).eventData(eventData)
                .performedBy(UUID.fromString(performedBy)).tenantId(claim.getTenantId()).build();
        claimHistoryRepository.save(history);
    }
}
