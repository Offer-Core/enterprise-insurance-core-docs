package com.enterprise.insurance.core.repository.claim;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.enterprise.insurance.core.domain.claim.Claim;
import com.enterprise.insurance.core.domain.claim.ClaimStatus;
import com.enterprise.insurance.core.domain.claim.ClaimType;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, UUID> {

    Optional<Claim> findByClaimNumber(String claimNumber);

    List<Claim> findByPolicyNumber(String policyNumber);

    List<Claim> findByCustomerId(UUID customerId);

    List<Claim> findByStatus(ClaimStatus status);

    List<Claim> findByHandlerId(UUID handlerId);

    List<Claim> findByClaimType(ClaimType claimType);

    List<Claim> findByLineOfBusiness(String lineOfBusiness);

    List<Claim> findByFraudReviewRequiredTrue();

    @Query("SELECT c FROM Claim c WHERE c.status NOT IN ('CLOSED', 'REJECTED', 'FRAUD_CONFIRMED')")
    List<Claim> findOpenClaims();

    @Query("SELECT c FROM Claim c WHERE c.incidentDate BETWEEN :from AND :to")
    List<Claim> findByIncidentDateBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT c FROM Claim c WHERE c.reportedDate BETWEEN :from AND :to")
    List<Claim> findByReportedDateBetween(@Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("SELECT c FROM Claim c WHERE c.tenantId = :tenantId")
    List<Claim> findByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT c FROM Claim c WHERE c.fraudScore >= :threshold")
    List<Claim> findByFraudScoreGreaterThanEqual(@Param("threshold") Double threshold);

    @Query("SELECT c FROM Claim c WHERE "
            + "(:claimNumber IS NULL OR c.claimNumber = :claimNumber) AND "
            + "(:policyNumber IS NULL OR c.policyNumber = :policyNumber) AND "
            + "(:customerId IS NULL OR c.customerId = :customerId) AND "
            + "(:status IS NULL OR c.status = :status) AND "
            + "(:claimType IS NULL OR c.claimType = :claimType) AND "
            + "(:lineOfBusiness IS NULL OR c.lineOfBusiness = :lineOfBusiness) AND "
            + "(:handlerId IS NULL OR c.handlerId = :handlerId) AND "
            + "(:fraudReviewRequired IS NULL OR c.fraudReviewRequired = :fraudReviewRequired) AND "
            + "(:tenantId IS NULL OR c.tenantId = :tenantId)")
    Page<Claim> searchClaims(@Param("claimNumber") String claimNumber,
            @Param("policyNumber") String policyNumber, @Param("customerId") UUID customerId,
            @Param("status") ClaimStatus status, @Param("claimType") ClaimType claimType,
            @Param("lineOfBusiness") String lineOfBusiness, @Param("handlerId") UUID handlerId,
            @Param("fraudReviewRequired") Boolean fraudReviewRequired,
            @Param("tenantId") String tenantId, Pageable pageable);

    long countByStatus(ClaimStatus status);

    long countByFraudReviewRequiredTrue();

    @Query("SELECT COUNT(c) FROM Claim c WHERE c.status NOT IN ('CLOSED', 'REJECTED', 'FRAUD_CONFIRMED')")
    long countOpenClaims();
}
