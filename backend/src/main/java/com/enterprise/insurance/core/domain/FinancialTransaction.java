package com.enterprise.insurance.core.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "financial_transactions", schema = "core",
        indexes = {@Index(name = "idx_fin_txn_policy", columnList = "policy_id"),
                @Index(name = "idx_fin_txn_claim", columnList = "claim_id"),
                @Index(name = "idx_fin_txn_status", columnList = "status"),
                @Index(name = "idx_fin_txn_tenant", columnList = "tenant_id"),
                @Index(name = "idx_fin_txn_recon", columnList = "reconciled", unique = false)})
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialTransaction extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "transaction_reference", length = 50, unique = true, nullable = false)
    private String transactionReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 30, nullable = false)
    private FinancialTransactionType transactionType;

    @Column(name = "policy_id")
    private UUID policyId;

    @Column(name = "claim_id")
    private UUID claimId;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    @Builder.Default
    private String currency = "SAR";

    @Column(name = "tax_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "fee_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal feeAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "gateway_reference", length = 100)
    private String gatewayReference;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "gateway_response", columnDefinition = "jsonb")
    private Map<String, Object> gatewayResponse;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    // Reconciliation
    @Builder.Default
    private Boolean reconciled = false;

    @Column(name = "reconciled_at")
    private LocalDateTime reconciledAt;

    @Column(name = "reconciliation_ref", length = 100)
    private String reconciliationRef;

    // Billing
    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "paid_date")
    private LocalDateTime paidDate;

    // Commission
    @Column(name = "commission_amount", precision = 15, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @Column(name = "agent_id")
    private UUID agentId;

    // SAMA compliance
    @Column(name = "sama_reference", length = 50)
    private String samaReference;

    @Column(name = "sama_reported")
    @Builder.Default
    private Boolean samaReported = false;

    // Description
    @Column(length = 500)
    private String description;

    @Column(name = "notes", length = 1000)
    private String notes;
}
