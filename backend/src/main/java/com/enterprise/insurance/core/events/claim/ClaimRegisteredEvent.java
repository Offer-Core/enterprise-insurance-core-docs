package com.enterprise.insurance.core.events.claim;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ClaimRegisteredEvent(UUID eventId, String claimNumber, String policyNumber,
        String customerId, String claimType, String lineOfBusiness, BigDecimal claimedAmount,
        LocalDate incidentDate, String status, String tenantId, Instant occurredAt) {
}
