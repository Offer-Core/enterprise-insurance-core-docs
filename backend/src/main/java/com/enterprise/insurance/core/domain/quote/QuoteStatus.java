package com.enterprise.insurance.core.domain.quote;

/**
 * Represents the lifecycle status of a quote. DRAFT → SUBMITTED → RATED → ACCEPTED → BOUND →
 * REJECTED → EXPIRED
 */
public enum QuoteStatus {
    DRAFT, // In progress, not submitted
    SUBMITTED, // Submitted for rating
    RATED, // Rating complete
    ACCEPTED, // Customer accepted
    REJECTED, // Customer rejected
    EXPIRED, // Quote expired (24 hours)
    BOUND; // Bound to a policy

    public boolean isTerminal() {
        return this == REJECTED || this == EXPIRED || this == BOUND;
    }

    public boolean isActive() {
        return this == DRAFT || this == SUBMITTED || this == RATED || this == ACCEPTED;
    }

    public boolean canTransitionTo(QuoteStatus target) {
        return switch (this) {
            case DRAFT -> target == SUBMITTED || target == REJECTED || target == EXPIRED;
            case SUBMITTED -> target == RATED || target == REJECTED || target == EXPIRED;
            case RATED -> target == ACCEPTED || target == REJECTED || target == EXPIRED;
            case ACCEPTED -> target == BOUND || target == REJECTED || target == EXPIRED;
            default -> false;
        };
    }
}
