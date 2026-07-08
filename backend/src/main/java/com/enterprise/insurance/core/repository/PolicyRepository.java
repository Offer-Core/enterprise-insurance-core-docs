package com.enterprise.insurance.core.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.enterprise.insurance.core.domain.Policy;
import com.enterprise.insurance.core.domain.PolicyStatus;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, UUID> {

    Optional<Policy> findByPolicyNumber(String policyNumber);

    List<Policy> findByCustomerId(UUID customerId);

    List<Policy> findByStatus(PolicyStatus status);

    List<Policy> findByLineOfBusiness(String lineOfBusiness);

    @Query("SELECT p FROM Policy p WHERE p.status IN :statuses")
    List<Policy> findByStatusIn(@Param("statuses") List<PolicyStatus> statuses);

    @Query("SELECT p FROM Policy p WHERE p.effectiveDate <= :date AND p.expiryDate >= :date")
    List<Policy> findActivePoliciesOnDate(@Param("date") LocalDate date);

    @Query("SELECT p FROM Policy p WHERE p.expiryDate BETWEEN :start AND :end")
    List<Policy> findExpiringBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT p FROM Policy p WHERE p.expiryDate BETWEEN :start AND :end AND p.status = :status")
    List<Policy> findByExpiryDateBetweenAndStatus(@Param("start") LocalDate start,
            @Param("end") LocalDate end, @Param("status") PolicyStatus status);

    @Query("SELECT p FROM Policy p WHERE p.expiryDate < :date AND p.status = 'ACTIVE'")
    List<Policy> findExpiredPolicies(@Param("date") LocalDate date);

    List<Policy> findByTenantId(String tenantId);

    @Query("SELECT p FROM Policy p WHERE p.customer.id = :customerId AND p.status = 'ACTIVE'")
    List<Policy> findActivePoliciesByCustomer(@Param("customerId") UUID customerId);

    long countByStatus(PolicyStatus status);

    @Query("SELECT COUNT(p) FROM Policy p WHERE p.status = 'ACTIVE' AND p.tenantId = :tenantId")
    long countActiveByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT p FROM Policy p WHERE p.samaReported = false AND p.status = 'ACTIVE'")
    List<Policy> findUnreportedToSama();
}
