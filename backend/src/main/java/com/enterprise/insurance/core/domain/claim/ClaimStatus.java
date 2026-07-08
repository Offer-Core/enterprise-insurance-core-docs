package com.enterprise.insurance.core.domain.claim;

/**
 * Represents the complete lifecycle status of a claim. Follows the state machine: REGISTERED →
 * ASSESSMENT → INVESTIGATING → ADJUDICATING → APPROVED/PARTIAL_APPROVED/REJECTED → PAYMENT_PENDING
 * → PAID → CLOSED → REOPENED → FRAUD_REVIEW → FRAUD_CONFIRMED
 */
public enum ClaimStatus {
    REGISTERED, ASSESSMENT, INVESTIGATING, ADJUDICATING, APPROVED, PARTIAL_APPROVED, REJECTED, PAYMENT_PENDING, PAID, CLOSED, REOPENED, FRAUD_REVIEW, FRAUD_CONFIRMED;

    public boolean isTerminal() {
        return this == CLOSED || this == REJECTED || this == FRAUD_CONFIRMED;
    }

    public boolean isActive() {
        return switch (this) {
            case REGISTERED, ASSESSMENT, INVESTIGATING, ADJUDICATING, APPROVED, PARTIAL_APPROVED, PAYMENT_PENDING, PAID, REOPENED, FRAUD_REVIEW -> true;
            default -> false;
        };
    }

    public boolean isPayable() {
        return this == APPROVED || this == PARTIAL_APPROVED;
    }

    public boolean canTransitionTo(ClaimStatus target) {
        return switch (this) {
            case REGISTERED -> target == ASSESSMENT || target == FRAUD_REVIEW || target == REJECTED
                    || target == CLOSED;
            case ASSESSMENT -> target == INVESTIGATING || target == ADJUDICATING
                    || target == FRAUD_REVIEW || target == REJECTED || target == CLOSED;
            case INVESTIGATING -> target == ADJUDICATING || target == FRAUD_REVIEW
                    || target == REJECTED || target == CLOSED;
            case ADJUDICATING -> target == APPROVED || target == PARTIAL_APPROVED
                    || target == REJECTED || target == FRAUD_REVIEW;
            case APPROVED -> target == PAYMENT_PENDING || target == REJECTED
                    || target == FRAUD_REVIEW || target == CLOSED;
            case PARTIAL_APPROVED -> target == PAYMENT_PENDING || target == REJECTED
                    || target == APPROVED || target == CLOSED;
            case REJECTED -> target == REOPENED || target == CLOSED;
            case PAYMENT_PENDING -> target == PAID || target == APPROVED || target == REJECTED
                    || target == FRAUD_REVIEW;
            case PAID -> target == CLOSED || target == REOPENED;
            case CLOSED -> target == REOPENED;
            case REOPENED -> target == ASSESSMENT || target == INVESTIGATING
                    || target == ADJUDICATING || target == FRAUD_REVIEW;
            case FRAUD_REVIEW -> target == FRAUD_CONFIRMED || target == ADJUDICATING
                    || target == REJECTED || target == CLOSED;
            case FRAUD_CONFIRMED -> target == CLOSED;
        };
    }
}
