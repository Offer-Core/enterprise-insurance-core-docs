package com.enterprise.insurance.core.events.claim;

import java.time.Instant;
import java.util.UUID;

public record ClaimInvestigationStartedEvent(UUID eventId, String claimNumber, String assignedTo,
        String investigationType, Instant occurredAt) {
}
