package com.enterprise.insurance.lines.motor.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.enterprise.insurance.core.domain.ContactInfo;
import com.enterprise.insurance.core.domain.Customer;
import com.enterprise.insurance.core.domain.IdentityType;
import com.enterprise.insurance.core.domain.LineOfBusiness;
import com.enterprise.insurance.core.domain.Policy;
import com.enterprise.insurance.core.domain.PolicyStatus;
import com.enterprise.insurance.core.exception.BusinessRuleException;
import com.enterprise.insurance.core.repository.CustomerRepository;
import com.enterprise.insurance.core.repository.PolicyRepository;
import com.enterprise.insurance.lines.motor.api.MotorPolicyResponse;
import com.enterprise.insurance.lines.motor.api.MotorQuoteRequest;
import com.enterprise.insurance.lines.motor.domain.MotorDriver;
import com.enterprise.insurance.lines.motor.domain.MotorQuoteResult;
import com.enterprise.insurance.lines.motor.domain.MotorVehicle;
import com.enterprise.insurance.lines.motor.infrastructure.client.NajmClient;
import com.enterprise.insurance.lines.motor.infrastructure.client.YakeenClient;
import com.enterprise.insurance.lines.motor.repository.MotorDriverRepository;
import com.enterprise.insurance.lines.motor.repository.MotorQuoteResultRepository;
import com.enterprise.insurance.lines.motor.repository.MotorVehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MotorPolicyService {

    private final CustomerRepository customerRepository;
    private final PolicyRepository policyRepository;
    private final MotorVehicleRepository motorVehicleRepository;
    private final MotorDriverRepository motorDriverRepository;
    private final MotorQuoteResultRepository motorQuoteResultRepository;
    private final NajmClient najmClient;
    private final YakeenClient yakeenClient;
    private final RatingEngineService ratingEngineService;

    @Transactional
    public MotorPolicyResponse createQuote(MotorQuoteRequest request) {
        log.info("Creating motor quote for nationalId: {}", request.getNationalId());

        // 1. Validate customer via Yakeen
        boolean isValid = yakeenClient.validateNationalId(request.getNationalId());
        if (!isValid) {
            throw new BusinessRuleException("INVALID_NATIONAL_ID",
                    "National ID validation failed with Yakeen");
        }

        // 2. Get or create customer
        Customer customer = customerRepository.findByNationalIdEncrypted(request.getNationalId())
                .orElseGet(() -> createCustomer(request));

        // 3. Check vehicle history via Najm
        var accidentHistory =
                najmClient.checkAccidentHistory(request.getVehicle().getChassisNumber());
        if (accidentHistory != null && accidentHistory.hasAccidents()) {
            log.warn("Vehicle has accident history: {}", accidentHistory);
        }

        // 4. Create policy
        Policy policy = Policy.builder().policyNumber(generatePolicyNumber())
                .productCode("MOTOR_TPL").lineOfBusiness(LineOfBusiness.MOTOR).customer(customer)
                .status(PolicyStatus.QUOTE).effectiveDate(LocalDate.now().plusDays(1))
                .expiryDate(LocalDate.now().plusYears(1)).annualPremium(BigDecimal.ZERO)
                .currency("SAR").build();
        Policy savedPolicy = policyRepository.save(policy);

        // 5. Save vehicle
        MotorVehicle vehicle = MotorVehicle.builder().policy(savedPolicy)
                .plateNumber(request.getVehicle().getPlateNumber())
                .vehicleMake(request.getVehicle().getVehicleMake())
                .vehicleModel(request.getVehicle().getVehicleModel())
                .vehicleYear(request.getVehicle().getVehicleYear())
                .chassisNumber(request.getVehicle().getChassisNumber()).build();
        motorVehicleRepository.save(vehicle);

        // 6. Save drivers
        if (request.getDrivers() != null) {
            request.getDrivers().forEach(driverRequest -> {
                MotorDriver driver = MotorDriver.builder().policy(savedPolicy).customer(customer)
                        .drivingLicenseNumber(driverRequest.getDrivingLicenseNumber())
                        .licenseExpiryDate(driverRequest.getLicenseExpiryDate())
                        .isPrimaryDriver(driverRequest.getIsPrimaryDriver()).build();
                motorDriverRepository.save(driver);
            });
        }

        // 7. Calculate premium
        BigDecimal finalPremium = ratingEngineService.calculatePremium(savedPolicy, vehicle);
        savedPolicy.setAnnualPremium(finalPremium);
        savedPolicy.calculateTotalPremium();
        policyRepository.save(savedPolicy);

        // 8. Save quote result
        MotorQuoteResult quoteResult = MotorQuoteResult.builder().policy(savedPolicy)
                .basePremium(finalPremium.multiply(BigDecimal.valueOf(0.8)))
                .finalPremium(finalPremium).expiresAt(LocalDate.now().plusDays(30).atStartOfDay())
                .build();
        motorQuoteResultRepository.save(quoteResult);

        return mapToResponse(savedPolicy, vehicle, finalPremium);
    }

    private Customer createCustomer(MotorQuoteRequest request) {
        ContactInfo contactInfo = ContactInfo.builder().mobileNumber(request.getMobile())
                .email(request.getEmail()).build();

        Customer customer = Customer.builder().customerNumber("CUST-" + System.currentTimeMillis())
                .nationalIdEncrypted(request.getNationalId()).identityType(IdentityType.CITIZEN)
                .fullNameAr(request.getFullNameAr()).dateOfBirthGregorian(request.getDateOfBirth())
                .contactInfo(contactInfo).build();
        return customerRepository.save(customer);
    }

    private String generatePolicyNumber() {
        return "MOT-" + System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private MotorPolicyResponse mapToResponse(Policy policy, MotorVehicle vehicle,
            BigDecimal finalPremium) {
        return MotorPolicyResponse.builder().policyId(policy.getId())
                .policyNumber(policy.getPolicyNumber()).status(policy.getStatus().name())
                .effectiveDate(policy.getEffectiveDate()).expiryDate(policy.getExpiryDate())
                .premiumAmount(policy.getAnnualPremium()).currency(policy.getCurrency())
                .finalPremium(finalPremium)
                .vehicle(MotorPolicyResponse.VehicleResponse.builder()
                        .plateNumber(vehicle.getPlateNumber()).vehicleMake(vehicle.getVehicleMake())
                        .vehicleModel(vehicle.getVehicleModel())
                        .vehicleYear(vehicle.getVehicleYear()).build())
                .build();
    }
}
