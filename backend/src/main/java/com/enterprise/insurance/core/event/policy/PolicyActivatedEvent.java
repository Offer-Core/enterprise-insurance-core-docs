package com.enterprise.insurance.core.event.policy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PolicyActivatedEvent(UUID eventId, String policyNumber, LocalDate activationDate,
        Instant occurredAt) {
    public PolicyActivatedEvent {
        eventId = eventId != null ? eventId : UUID.randomUUID();
        occurredAt = occurredAt != null ? occurredAt : Instant.now();
    }
}
