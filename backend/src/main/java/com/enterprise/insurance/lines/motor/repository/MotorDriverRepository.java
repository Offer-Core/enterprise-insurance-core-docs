package com.enterprise.insurance.lines.motor.repository;

import com.enterprise.insurance.core.repository.BaseRepository;
import com.enterprise.insurance.lines.motor.domain.MotorDriver;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MotorDriverRepository extends BaseRepository<MotorDriver> {
    List<MotorDriver> findByPolicyId(UUID policyId);
    List<MotorDriver> findByPolicyIdAndIsPrimaryDriverTrue(UUID policyId);
}
