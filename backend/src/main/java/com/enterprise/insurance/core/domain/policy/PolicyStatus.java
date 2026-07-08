package com.enterprise.insurance.core.domain.policy;

/**
 * Represents the complete lifecycle status of a policy. Follows the state machine: QUOTE → BOUND →
 * ACTIVE → ISSUED → RENEWED → EXPIRED → CANCELLED → LAPSED → SUSPENDED → ENDORSED
 */
public enum PolicyStatus {
    QUOTE, // Initial quote, not yet accepted
    BOUND, // Accepted but not yet active
    ACTIVE, // Coverage active
    ISSUED, // Policy document issued
    RENEWED, // Renewed for another term
    CANCELLED, // Cancelled before expiry
    EXPIRED, // Reached expiry date
    LAPSED, // Lapsed due to non-payment
    SUSPENDED, // Temporarily suspended
    ENDORSED; // Has endorsements

    public boolean isTerminal() {
        return this == CANCELLED || this == EXPIRED || this == LAPSED;
    }

    public boolean isActive() {
        return this == ACTIVE || this == ISSUED || this == RENEWED || this == ENDORSED;
    }

    public boolean canTransitionTo(PolicyStatus target) {
        return switch (this) {
            case QUOTE -> target == BOUND || target == CANCELLED || target == EXPIRED;
            case BOUND -> target == ACTIVE || target == CANCELLED || target == EXPIRED
                    || target == LAPSED;
            case ACTIVE -> target == ISSUED || target == RENEWED || target == CANCELLED
                    || target == EXPIRED || target == LAPSED || target == SUSPENDED
                    || target == ENDORSED;
            case ISSUED -> target == RENEWED || target == CANCELLED || target == EXPIRED
                    || target == LAPSED || target == ENDORSED;
            case RENEWED -> target == ACTIVE || target == CANCELLED || target == EXPIRED;
            case SUSPENDED -> target == ACTIVE || target == CANCELLED || target == EXPIRED;
            case ENDORSED -> target == ACTIVE || target == ISSUED || target == CANCELLED
                    || target == EXPIRED || target == LAPSED;
            default -> false;
        };
    }
}
