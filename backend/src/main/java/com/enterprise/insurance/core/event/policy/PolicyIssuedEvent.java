package com.enterprise.insurance.core.event.policy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PolicyIssuedEvent(UUID eventId, String policyNumber, String productCode,
        String policyDocumentUrl, LocalDate issuedDate, Instant occurredAt) {
    public PolicyIssuedEvent {
        eventId = eventId != null ? eventId : UUID.randomUUID();
        occurredAt = occurredAt != null ? occurredAt : Instant.now();
    }
}
