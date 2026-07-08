package com.enterprise.insurance.core.event.policy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PolicyCancelledEvent(UUID eventId, String policyNumber, String cancellationReason,
        LocalDate cancellationDate, BigDecimal refundAmount, Instant occurredAt) {
    public PolicyCancelledEvent {
        eventId = eventId != null ? eventId : UUID.randomUUID();
        occurredAt = occurredAt != null ? occurredAt : Instant.now();
    }
}
