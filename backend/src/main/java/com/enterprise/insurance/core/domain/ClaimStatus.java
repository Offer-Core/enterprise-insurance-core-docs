package com.enterprise.insurance.core.domain;

public enum ClaimStatus {
    REPORTED, INVESTIGATING, RESERVED, ADJUDICATED, PAID, CLOSED, REJECTED, APPEALED;

    public boolean isTerminal() {
        return this == CLOSED || this == REJECTED;
    }

    public boolean canTransitionTo(ClaimStatus target) {
        return switch (this) {
            case REPORTED -> target == INVESTIGATING || target == RESERVED || target == REJECTED;
            case INVESTIGATING -> target == RESERVED || target == ADJUDICATED || target == REJECTED;
            case RESERVED -> target == ADJUDICATED || target == PAID;
            case ADJUDICATED -> target == PAID || target == APPEALED || target == CLOSED;
            case PAID -> target == CLOSED;
            case APPEALED -> target == INVESTIGATING || target == ADJUDICATED;
            default -> false;
        };
    }
}
