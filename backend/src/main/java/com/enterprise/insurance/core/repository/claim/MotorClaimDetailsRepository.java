package com.enterprise.insurance.core.repository.claim;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.enterprise.insurance.core.domain.claim.MotorClaimDetails;

@Repository
public interface MotorClaimDetailsRepository extends JpaRepository<MotorClaimDetails, UUID> {

    Optional<MotorClaimDetails> findByClaimId(UUID claimId);

    Optional<MotorClaimDetails> findByPoliceReportNumber(String policeReportNumber);
}
