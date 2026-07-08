package com.enterprise.insurance.core.event.policy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PolicyEndorsedEvent(UUID eventId, String policyNumber, String endorsementNumber,
        String endorsementType, BigDecimal premiumAdjustment, LocalDate effectiveDate,
        Instant occurredAt) {
    public PolicyEndorsedEvent {
        eventId = eventId != null ? eventId : UUID.randomUUID();
        occurredAt = occurredAt != null ? occurredAt : Instant.now();
    }
}
