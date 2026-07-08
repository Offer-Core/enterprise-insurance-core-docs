package com.enterprise.insurance.core.event.policy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PolicyCreatedEvent(UUID eventId, String policyNumber, String productCode,
        UUID customerId, BigDecimal annualPremium, LocalDate effectiveDate, LocalDate expiryDate,
        String lineOfBusiness, String tenantId, Instant occurredAt) {
    public PolicyCreatedEvent {
        eventId = eventId != null ? eventId : UUID.randomUUID();
        occurredAt = occurredAt != null ? occurredAt : Instant.now();
    }
}
