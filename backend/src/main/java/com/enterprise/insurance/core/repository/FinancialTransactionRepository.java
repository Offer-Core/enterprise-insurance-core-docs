package com.enterprise.insurance.core.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.enterprise.insurance.core.domain.FinancialTransaction;
import com.enterprise.insurance.core.domain.TransactionStatus;

@Repository
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, UUID> {

    Optional<FinancialTransaction> findByTransactionReference(String transactionReference);

    Optional<FinancialTransaction> findByGatewayReference(String gatewayReference);

    List<FinancialTransaction> findByPolicyId(UUID policyId);

    List<FinancialTransaction> findByClaimId(UUID claimId);

    List<FinancialTransaction> findByCustomerId(UUID customerId);

    List<FinancialTransaction> findByStatus(TransactionStatus status);

    List<FinancialTransaction> findByTenantId(String tenantId);

    @Query("SELECT t FROM FinancialTransaction t WHERE t.reconciled = false AND t.status = 'COMPLETED'")
    List<FinancialTransaction> findUnreconciledTransactions();

    @Query("SELECT t FROM FinancialTransaction t WHERE t.dueDate < :now AND t.status = 'PENDING'")
    List<FinancialTransaction> findOverduePayments(@Param("now") LocalDateTime now);

    @Query("SELECT SUM(t.amount) FROM FinancialTransaction t WHERE t.policyId = :policyId AND t.status = 'COMPLETED'")
    Double totalPaidForPolicy(@Param("policyId") UUID policyId);

    @Query("SELECT t FROM FinancialTransaction t WHERE t.samaReported = false AND t.status = 'COMPLETED'")
    List<FinancialTransaction> findUnreportedToSama();
}
