package com.enterprise.insurance.lines.motor.application;

import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import com.enterprise.insurance.core.domain.Customer;
import com.enterprise.insurance.core.domain.Policy;
import com.enterprise.insurance.lines.motor.api.MotorPolicyResponse;
import com.enterprise.insurance.lines.motor.api.MotorQuoteRequest;
import com.enterprise.insurance.lines.motor.domain.MotorDriver;
import com.enterprise.insurance.lines.motor.domain.MotorVehicle;

@Mapper
public interface MotorPolicyMapper {

    MotorPolicyMapper INSTANCE = Mappers.getMapper(MotorPolicyMapper.class);

    Customer toCustomer(MotorQuoteRequest request);

    @Mapping(source = "request.vehicle", target = ".")
    MotorVehicle toMotorVehicle(MotorQuoteRequest request);

    @Mapping(source = "request", target = ".")
    MotorDriver toMotorDriver(MotorQuoteRequest.DriverRequest request);

    @Mapping(source = "policy.id", target = "policyId")
    @Mapping(source = "policy.policyNumber", target = "policyNumber")
    @Mapping(source = "policy.status", target = "status")
    @Mapping(source = "policy.effectiveDate", target = "effectiveDate")
    @Mapping(source = "policy.expiryDate", target = "expiryDate")
    @Mapping(source = "policy.premiumAmount", target = "premiumAmount")
    @Mapping(source = "policy.currency", target = "currency")
    @Mapping(source = "vehicle", target = "vehicle")
    @Mapping(source = "finalPremium", target = "finalPremium")
    MotorPolicyResponse toResponse(Policy policy, MotorVehicle vehicle, BigDecimal finalPremium);
}
