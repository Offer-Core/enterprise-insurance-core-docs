package com.enterprise.insurance.core.repository.policy;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.enterprise.insurance.core.domain.Policy;
import com.enterprise.insurance.core.domain.PolicyStatus;

@Repository
public interface PolicySearchRepository extends JpaRepository<Policy, UUID> {

    List<Policy> findByCustomerId(UUID customerId);

    List<Policy> findByStatus(PolicyStatus status);

    List<Policy> findByAgentId(UUID agentId);

    List<Policy> findByProductCode(String productCode);

    @Query("SELECT p FROM Policy p WHERE p.status = 'ACTIVE' OR p.status = 'ISSUED'")
    List<Policy> findActivePolicies();

    @Query("SELECT p FROM Policy p WHERE p.expiryDate BETWEEN :from AND :to AND p.status = 'ACTIVE'")
    List<Policy> findExpiringBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT p FROM Policy p WHERE p.expiryDate <= :date AND p.status = 'ACTIVE'")
    List<Policy> findExpiredActivePolicies(@Param("date") LocalDate date);

    @Query("SELECT p FROM Policy p WHERE p.createdAt >= :daysAgo ORDER BY p.createdAt DESC")
    List<Policy> findRecentPolicies(@Param("daysAgo") java.time.LocalDateTime daysAgo,
            Pageable pageable);

    @Query("SELECT p FROM Policy p WHERE p.effectiveDate >= :from AND p.effectiveDate <= :to")
    List<Policy> findByEffectiveDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT p FROM Policy p WHERE p.expiryDate >= :from AND p.expiryDate <= :to")
    List<Policy> findByExpiryDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT p FROM Policy p WHERE p.customer.id = :customerId AND p.status IN :statuses")
    List<Policy> findByCustomerAndStatuses(@Param("customerId") UUID customerId,
            @Param("statuses") List<PolicyStatus> statuses);

    @Query("SELECT p FROM Policy p WHERE "
            + "(:policyNumber IS NULL OR p.policyNumber LIKE %:policyNumber%) AND "
            + "(:customerId IS NULL OR p.customer.id = :customerId) AND "
            + "(:status IS NULL OR p.status = :status) AND "
            + "(:productCode IS NULL OR p.productCode = :productCode) AND "
            + "(:effectiveFrom IS NULL OR p.effectiveDate >= :effectiveFrom) AND "
            + "(:effectiveTo IS NULL OR p.effectiveDate <= :effectiveTo)")
    Page<Policy> searchPolicies(@Param("policyNumber") String policyNumber,
            @Param("customerId") UUID customerId, @Param("status") PolicyStatus status,
            @Param("productCode") String productCode,
            @Param("effectiveFrom") LocalDate effectiveFrom,
            @Param("effectiveTo") LocalDate effectiveTo, Pageable pageable);
}
