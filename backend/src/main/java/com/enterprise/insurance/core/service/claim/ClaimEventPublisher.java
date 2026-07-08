package com.enterprise.insurance.core.service.claim;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.enterprise.insurance.core.domain.claim.Claim;
import com.enterprise.insurance.core.events.claim.ClaimApprovedEvent;
import com.enterprise.insurance.core.events.claim.ClaimClosedEvent;
import com.enterprise.insurance.core.events.claim.ClaimInvestigationStartedEvent;
import com.enterprise.insurance.core.events.claim.ClaimPaidEvent;
import com.enterprise.insurance.core.events.claim.ClaimRegisteredEvent;
import com.enterprise.insurance.core.events.claim.ClaimRejectedEvent;
import com.enterprise.insurance.core.events.claim.ClaimStatusChangedEvent;
import com.enterprise.insurance.core.events.claim.FraudAlertEvent;

@Component
public class ClaimEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ClaimEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.claim-registered:claim-registered}")
    private String claimRegisteredTopic;

    @Value("${app.kafka.topics.claim-status-changed:claim-status-changed}")
    private String claimStatusChangedTopic;

    @Value("${app.kafka.topics.claim-approved:claim-approved}")
    private String claimApprovedTopic;

    @Value("${app.kafka.topics.claim-rejected:claim-rejected}")
    private String claimRejectedTopic;

    @Value("${app.kafka.topics.claim-paid:claim-paid}")
    private String claimPaidTopic;

    @Value("${app.kafka.topics.claim-closed:claim-closed}")
    private String claimClosedTopic;

    @Value("${app.kafka.topics.claim-investigation-started:claim-investigation-started}")
    private String claimInvestigationStartedTopic;

    @Value("${app.kafka.topics.fraud-alert:fraud-alert}")
    private String fraudAlertTopic;

    public ClaimEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishClaimRegistered(Claim claim) {
        var event = new ClaimRegisteredEvent(UUID.randomUUID(), claim.getClaimNumber(),
                claim.getPolicyNumber(), claim.getCustomerId().toString(),
                claim.getClaimType().name(), claim.getLineOfBusiness(), claim.getClaimedAmount(),
                claim.getIncidentDate(), claim.getStatus().name(), claim.getTenantId(),
                Instant.now());
        publish(claimRegisteredTopic, claim.getClaimNumber(), event);
        log.info("Published ClaimRegisteredEvent for claim: {}", claim.getClaimNumber());
    }

    public void publishClaimStatusChanged(Claim claim, String previousStatus, String reason,
            String changedBy) {
        var event = new ClaimStatusChangedEvent(UUID.randomUUID(), claim.getClaimNumber(),
                previousStatus, claim.getStatus().name(), reason, changedBy, Instant.now());
        publish(claimStatusChangedTopic, claim.getClaimNumber(), event);
        log.info("Published ClaimStatusChangedEvent for claim: {} ({} -> {})",
                claim.getClaimNumber(), previousStatus, claim.getStatus());
    }

    public void publishClaimApproved(Claim claim, String approvedBy, String reason) {
        var event = new ClaimApprovedEvent(UUID.randomUUID(), claim.getClaimNumber(),
                claim.getApprovedAmount(), claim.getClaimedAmount(), approvedBy, reason,
                Instant.now());
        publish(claimApprovedTopic, claim.getClaimNumber(), event);
        log.info("Published ClaimApprovedEvent for claim: {}", claim.getClaimNumber());
    }

    public void publishClaimRejected(Claim claim, String rejectedBy, String reason) {
        var event = new ClaimRejectedEvent(UUID.randomUUID(), claim.getClaimNumber(), reason,
                rejectedBy, Instant.now());
        publish(claimRejectedTopic, claim.getClaimNumber(), event);
        log.info("Published ClaimRejectedEvent for claim: {}", claim.getClaimNumber());
    }

    public void publishClaimPaid(Claim claim, String transactionId, String paymentMethod,
            String paidBy) {
        var event = new ClaimPaidEvent(UUID.randomUUID(), claim.getClaimNumber(),
                claim.getPaidAmount(), transactionId, paymentMethod, paidBy, Instant.now());
        publish(claimPaidTopic, claim.getClaimNumber(), event);
        log.info("Published ClaimPaidEvent for claim: {}", claim.getClaimNumber());
    }

    public void publishClaimClosed(Claim claim, String closedBy) {
        var event = new ClaimClosedEvent(UUID.randomUUID(), claim.getClaimNumber(),
                claim.getPaidAmount(), claim.getSettlementDays(), closedBy, Instant.now());
        publish(claimClosedTopic, claim.getClaimNumber(), event);
        log.info("Published ClaimClosedEvent for claim: {}", claim.getClaimNumber());
    }

    public void publishClaimInvestigationStarted(Claim claim, String assignedTo) {
        var event = new ClaimInvestigationStartedEvent(UUID.randomUUID(), claim.getClaimNumber(),
                assignedTo, "STANDARD", Instant.now());
        publish(claimInvestigationStartedTopic, claim.getClaimNumber(), event);
        log.info("Published ClaimInvestigationStartedEvent for claim: {}", claim.getClaimNumber());
    }

    public void publishFraudAlert(Claim claim) {
        var event = new FraudAlertEvent(UUID.randomUUID(), claim.getClaimNumber(),
                claim.getFraudScore(), getRiskLevel(claim.getFraudScore()),
                claim.getFraudIndicators(), getAlertLevel(claim.getFraudScore()), Instant.now());
        publish(fraudAlertTopic, claim.getClaimNumber(), event);
        log.info("Published FraudAlertEvent for claim: {} (score: {})", claim.getClaimNumber(),
                claim.getFraudScore());
    }

    private void publish(String topic, String key, Object event) {
        kafkaTemplate.send(topic, key, event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event to topic {}: {}", topic, ex.getMessage());
            }
        });
    }

    private String getRiskLevel(BigDecimal score) {
        if (score == null)
            return "LOW";
        if (score.compareTo(new BigDecimal("0.75")) >= 0)
            return "CRITICAL";
        if (score.compareTo(new BigDecimal("0.50")) >= 0)
            return "HIGH";
        if (score.compareTo(new BigDecimal("0.25")) >= 0)
            return "MEDIUM";
        return "LOW";
    }

    private String getAlertLevel(BigDecimal score) {
        if (score == null)
            return "INFO";
        if (score.compareTo(new BigDecimal("0.75")) >= 0)
            return "CRITICAL";
        if (score.compareTo(new BigDecimal("0.50")) >= 0)
            return "WARNING";
        return "INFO";
    }
}
