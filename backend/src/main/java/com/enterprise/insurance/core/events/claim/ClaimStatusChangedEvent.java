package com.enterprise.insurance.core.events.claim;

import java.time.Instant;
import java.util.UUID;

public record ClaimStatusChangedEvent(UUID eventId, String claimNumber, String previousStatus,
        String newStatus, String reason, String changedBy, Instant occurredAt) {
}
