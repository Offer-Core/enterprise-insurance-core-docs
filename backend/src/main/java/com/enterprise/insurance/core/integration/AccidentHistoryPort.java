package com.enterprise.insurance.core.integration;

import java.time.LocalDate;
import java.util.List;

/**
 * Port interface for accident history via Najm (Saudi national system).
 */
public interface AccidentHistoryPort {

    /**
     * Get accident history for a vehicle.
     *
     * @param sequenceNumber Vehicle sequence number
     * @return accident history result
     */
    VehicleAccidentHistory getVehicleHistory(String sequenceNumber);

    /**
     * Get accident history for a driver.
     *
     * @param nationalId Driver's National ID
     * @return accident history result
     */
    DriverAccidentHistory getDriverHistory(String nationalId);

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class VehicleAccidentHistory {
        private String sequenceNumber;
        private int totalAccidents;
        private int atFaultAccidents;
        private int notAtFaultAccidents;
        private List<AccidentRecord> accidents;
        private String najmReference;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class DriverAccidentHistory {
        private String nationalId;
        private int totalAccidents;
        private int atFaultAccidents;
        private List<AccidentRecord> accidents;
        private String najmReference;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class AccidentRecord {
        private String accidentReference;
        private LocalDate accidentDate;
        private String accidentType;
        private String accidentLocation;
        private boolean atFault;
        private String faultPercentage;
        private String vehicleSequenceNumber;
        private String otherPartyNationalId;
        private String otherPartyInsuranceCompany;
        private String policeReportNumber;
        private String description;
    }
}
