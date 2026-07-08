package com.enterprise.insurance.core.domain.endorsement;

public enum EndorsementType {
    VEHICLE_CHANGE, // Change vehicle
    DRIVER_ADDITION, // Add driver
    DRIVER_REMOVAL, // Remove driver
    COVERAGE_CHANGE, // Change coverage
    LIMIT_CHANGE, // Change limit
    ADDRESS_CHANGE, // Change address
    PAYMENT_CHANGE, // Change payment method
    NAME_CHANGE, // Change insured name
    DEDUCTIBLE_CHANGE, // Change deductible
    BENEFICIARY_CHANGE, // Change beneficiary
    CUSTOM // Custom endorsement
}
