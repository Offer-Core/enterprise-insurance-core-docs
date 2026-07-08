cat > src/main/java/com/enterprise/insurance/lines/motor/infrastructure/client/AccidentHistory.java << 'EOF'
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
EOF
