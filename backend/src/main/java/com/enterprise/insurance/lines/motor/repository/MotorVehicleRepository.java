package com.enterprise.insurance.lines.motor.repository;

import com.enterprise.insurance.core.repository.BaseRepository;
import com.enterprise.insurance.lines.motor.domain.MotorVehicle;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MotorVehicleRepository extends BaseRepository<MotorVehicle> {
    List<MotorVehicle> findByPolicyId(UUID policyId);
    Optional<MotorVehicle> findByChassisNumber(String chassisNumber);
    boolean existsByChassisNumber(String chassisNumber);
}
