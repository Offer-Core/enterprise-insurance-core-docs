package com.enterprise.insurance.lines.motor.repository;

import com.enterprise.insurance.core.repository.BaseRepository;
import com.enterprise.insurance.lines.motor.domain.MotorQuoteResult;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MotorQuoteResultRepository extends BaseRepository<MotorQuoteResult> {
    Optional<MotorQuoteResult> findByPolicyId(UUID policyId);
}
