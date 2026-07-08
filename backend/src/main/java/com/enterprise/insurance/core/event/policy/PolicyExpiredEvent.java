package com.enterprise.insurance.core.event.policy;

import java.time.Instant;
import java.util.UUID;

public record PolicyExpiredEvent(UUID eventId, String policyNumber, Instant occurredAt) {
    public PolicyExpiredEvent {
        eventId = eventId != null ? eventId : UUID.randomUUID();
        occurredAt = occurredAt != null ? occurredAt : Instant.now();
    }
}
