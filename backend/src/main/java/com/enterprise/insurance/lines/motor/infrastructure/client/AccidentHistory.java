package com.enterprise.insurance.lines.motor.infrastructure.client;

public record AccidentHistory(
    String vehicleId,
    boolean hasAccidents,
    int accidentCount,
    String lastAccidentDate
) {
    public boolean hasAccidents() {
        return hasAccidents;
    }
}
