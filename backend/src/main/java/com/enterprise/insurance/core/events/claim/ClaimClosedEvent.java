package com.enterprise.insurance.core.events.claim;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ClaimClosedEvent(UUID eventId, String claimNumber, BigDecimal totalPaid,
        Integer settlementDays, String closedBy, Instant occurredAt) {
}
