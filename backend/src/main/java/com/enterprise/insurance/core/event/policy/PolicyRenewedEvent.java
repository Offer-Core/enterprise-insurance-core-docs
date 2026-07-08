package com.enterprise.insurance.core.event.policy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PolicyRenewedEvent(UUID eventId, String oldPolicyNumber, String newPolicyNumber,
        BigDecimal newPremium, LocalDate newEffectiveDate, LocalDate newExpiryDate,
        Instant occurredAt) {
    public PolicyRenewedEvent {
        eventId = eventId != null ? eventId : UUID.randomUUID();
        occurredAt = occurredAt != null ? occurredAt : Instant.now();
    }
}
