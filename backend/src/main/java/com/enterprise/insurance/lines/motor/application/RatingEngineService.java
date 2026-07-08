package com.enterprise.insurance.lines.motor.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.enterprise.insurance.core.domain.Policy;
import com.enterprise.insurance.lines.motor.domain.MotorVehicle;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RatingEngineService {

    private static final BigDecimal BASE_RATE = BigDecimal.valueOf(500.00);

    public BigDecimal calculatePremium(Policy policy, MotorVehicle vehicle) {
        log.info("Calculating premium for policy: {}", policy.getPolicyNumber());

        Map<String, BigDecimal> factors = new HashMap<>();

        // Age factor (younger = higher risk)
        int age = calculateAge(policy.getCustomer().getDateOfBirthGregorian());
        BigDecimal ageFactor = getAgeFactor(age);

        // Vehicle factor
        BigDecimal vehicleFactor = getVehicleFactor(vehicle);

        // Discounts
        BigDecimal discount = BigDecimal.ZERO;

        // If no claims history (simplified)
        if (policy.getCustomer().getNationalIdEncrypted() != null) {
            discount = discount.add(BigDecimal.valueOf(0.10)); // 10% no-claims discount
        }

        BigDecimal basePremium = BASE_RATE.multiply(ageFactor).multiply(vehicleFactor);
        BigDecimal finalPremium = basePremium.multiply(BigDecimal.ONE.subtract(discount));

        log.info(
                "Calculated premium: {} (base: {}, ageFactor: {}, vehicleFactor: {}, discount: {})",
                finalPremium, basePremium, ageFactor, vehicleFactor, discount);

        return finalPremium.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private int calculateAge(LocalDate birthDate) {
        if (birthDate == null)
            return 30; // Default age if not provided
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private BigDecimal getAgeFactor(int age) {
        if (age < 25)
            return BigDecimal.valueOf(1.50);
        if (age < 30)
            return BigDecimal.valueOf(1.20);
        if (age < 50)
            return BigDecimal.valueOf(1.00);
        return BigDecimal.valueOf(1.10);
    }

    private BigDecimal getVehicleFactor(MotorVehicle vehicle) {
        int year = vehicle.getVehicleYear();
        int currentYear = LocalDate.now().getYear();

        if (currentYear - year < 3) {
            return BigDecimal.valueOf(1.30); // New car = higher premium
        } else if (currentYear - year < 7) {
            return BigDecimal.valueOf(1.00);
        } else {
            return BigDecimal.valueOf(0.80); // Old car = lower premium
        }
    }
}
