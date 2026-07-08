package com.enterprise.insurance.core.domain.endorsement;

public enum EndorsementStatus {
    REQUESTED, // Initial request
    REVIEWING, // Under review
    APPROVED, // Approved
    REJECTED, // Rejected
    EFFECTIVE, // Change effective
    CANCELLED // Cancelled before effective
}
