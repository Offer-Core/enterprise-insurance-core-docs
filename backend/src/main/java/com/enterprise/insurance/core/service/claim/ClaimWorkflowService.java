package com.enterprise.insurance.core.service.claim;

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
import com.enterprise.insurance.core.domain.claim.ClaimWorkflowDefinition;
import com.enterprise.insurance.core.domain.claim.ClaimWorkflowDefinition.WorkflowState;
import com.enterprise.insurance.core.domain.claim.ClaimWorkflowDefinition.WorkflowTransition;
import com.enterprise.insurance.core.exception.EntityNotFoundException;
import com.enterprise.insurance.core.repository.claim.ClaimHistoryRepository;
import com.enterprise.insurance.core.repository.claim.ClaimRepository;
import com.enterprise.insurance.core.repository.claim.ClaimWorkflowRepository;

@Service
@Transactional
public class ClaimWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(ClaimWorkflowService.class);

    private final ClaimRepository claimRepository;
    private final ClaimHistoryRepository claimHistoryRepository;
    private final ClaimWorkflowRepository workflowRepository;
    private final ClaimEventPublisher eventPublisher;

    public ClaimWorkflowService(ClaimRepository claimRepository,
            ClaimHistoryRepository claimHistoryRepository,
            ClaimWorkflowRepository workflowRepository, ClaimEventPublisher eventPublisher) {
        this.claimRepository = claimRepository;
        this.claimHistoryRepository = claimHistoryRepository;
        this.workflowRepository = workflowRepository;
        this.eventPublisher = eventPublisher;
    }

    public ClaimWorkflowDefinition getWorkflow(String claimNumber) {
        Claim claim = getClaim(claimNumber);
        return workflowRepository
                .findByLineOfBusinessAndClaimTypeAndIsDefaultTrue(claim.getLineOfBusiness(),
                        claim.getClaimType().name())
                .orElseThrow(() -> new EntityNotFoundException("No workflow found for "
                        + claim.getLineOfBusiness() + "/" + claim.getClaimType()));
    }

    public Claim transitionState(String claimNumber, String action, String performedBy) {
        Claim claim = getClaim(claimNumber);
        ClaimWorkflowDefinition workflow = getWorkflow(claimNumber);

        // Find valid transition
        WorkflowTransition transition = workflow.getTransitions().stream()
                .filter(t -> t.getFromState().equals(claim.getStatus().name())
                        && t.getAction().equals(action))
                .findFirst().orElseThrow(() -> new IllegalStateException("No valid transition from "
                        + claim.getStatus() + " with action " + action));

        ClaimStatus targetStatus = ClaimStatus.valueOf(transition.getToState());
        String previousStatus = claim.getStatus().name();

        claim.transitionTo(targetStatus);
        claim = claimRepository.save(claim);

        saveHistory(claim, "WORKFLOW_TRANSITION", previousStatus, targetStatus.name(),
                Map.of("action", action, "transition", transition.getToState()), performedBy);

        eventPublisher.publishClaimStatusChanged(claim, previousStatus,
                "Workflow transition: " + action, performedBy);

        log.info("Claim {} transitioned from {} to {} via action {}", claimNumber, previousStatus,
                targetStatus, action);
        return claim;
    }

    public List<String> getAvailableActions(String claimNumber) {
        Claim claim = getClaim(claimNumber);
        ClaimWorkflowDefinition workflow = getWorkflow(claimNumber);

        return workflow.getTransitions().stream()
                .filter(t -> t.getFromState().equals(claim.getStatus().name()))
                .map(WorkflowTransition::getAction).toList();
    }

    public List<ClaimHistory> getWorkflowHistory(String claimNumber) {
        Claim claim = getClaim(claimNumber);
        return claimHistoryRepository.findByClaimIdOrderByOccurredAtAsc(claim.getId());
    }

    public SLAStatus getSLAStatus(String claimNumber) {
        Claim claim = getClaim(claimNumber);
        ClaimWorkflowDefinition workflow = getWorkflow(claimNumber);

        WorkflowState currentState = workflow.getStates().stream()
                .filter(s -> s.getStateCode().equals(claim.getStatus().name())).findFirst()
                .orElse(null);

        if (currentState == null || currentState.getSlaDays() == null) {
            return new SLAStatus("NO_SLA", 0, 0, "OK");
        }

        long daysInState = claim.getUpdatedAt() != null ? java.time.Duration
                .between(claim.getUpdatedAt(), java.time.LocalDateTime.now()).toDays() : 0;

        int slaDays = currentState.getSlaDays();
        String status =
                daysInState > slaDays ? "BREACHED" : daysInState > slaDays * 0.8 ? "WARNING" : "OK";

        return new SLAStatus(currentState.getStateCode(), slaDays, (int) daysInState, status);
    }

    public record SLAStatus(String stateCode, int slaDays, int daysInState, String status) {
        public boolean isBreached() {
            return "BREACHED".equals(status);
        }
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
