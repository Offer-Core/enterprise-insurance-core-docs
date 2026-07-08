package com.enterprise.insurance.core.events.claim;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ClaimPaidEvent(UUID eventId, String claimNumber, BigDecimal paidAmount,
        String transactionId, String paymentMethod, String paidBy, Instant occurredAt) {
}
