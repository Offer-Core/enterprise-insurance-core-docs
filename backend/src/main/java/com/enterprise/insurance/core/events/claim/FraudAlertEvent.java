package com.enterprise.insurance.core.events.claim;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FraudAlertEvent(UUID eventId, String claimNumber, BigDecimal fraudScore,
        String riskLevel, List<String> fraudIndicators, String alertLevel, Instant occurredAt) {
}
