package com.enterprise.insurance.core.events.claim;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ClaimApprovedEvent(UUID eventId, String claimNumber, BigDecimal approvedAmount,
        BigDecimal claimedAmount, String approvedBy, String reason, Instant occurredAt) {
}
