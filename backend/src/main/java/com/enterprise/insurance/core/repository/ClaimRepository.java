package com.enterprise.insurance.core.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.enterprise.insurance.core.domain.Claim;
import com.enterprise.insurance.core.domain.ClaimStatus;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, UUID> {

    Optional<Claim> findByClaimNumber(String claimNumber);

    List<Claim> findByPolicyId(UUID policyId);

    List<Claim> findByCustomerId(UUID customerId);

    List<Claim> findByStatus(ClaimStatus status);

    List<Claim> findByHandlerId(UUID handlerId);

    List<Claim> findByTenantId(String tenantId);

    @Query("SELECT c FROM Claim c WHERE c.fraudReviewRequired = true")
    List<Claim> findFraudReviewRequired();

    @Query("SELECT c FROM Claim c WHERE c.samaReported = false AND c.status != 'REPORTED'")
    List<Claim> findUnreportedToSama();

    long countByStatus(ClaimStatus status);

    @Query("SELECT AVG(c.approvedAmount) FROM Claim c WHERE c.status = 'PAID'")
    Double averageClaimAmount();

    @Query("SELECT COUNT(c) FROM Claim c WHERE c.status = 'PAID' AND c.paidAmount IS NOT NULL")
    long countSettledClaims();
}
