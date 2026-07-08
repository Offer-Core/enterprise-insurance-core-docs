package com.enterprise.insurance.core.events.claim;

import java.time.Instant;
import java.util.UUID;

public record ClaimRejectedEvent(UUID eventId, String claimNumber, String reason, String rejectedBy,
        Instant occurredAt) {
}
