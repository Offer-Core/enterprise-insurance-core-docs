package com.enterprise.insurance.lines.motor.domain;

import com.enterprise.insurance.core.domain.Policy;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "motor_quote_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MotorQuoteResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(name = "base_premium", precision = 12, scale = 2)
    private BigDecimal basePremium;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> riskFactors;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> discounts;

    @Column(name = "final_premium", precision = 12, scale = 2, nullable = false)
    private BigDecimal finalPremium;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        calculatedAt = LocalDateTime.now();
    }
}
