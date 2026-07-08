package com.enterprise.insurance.core.domain.claim;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "claim_history", schema = "core",
        indexes = {@Index(name = "idx_claim_history_claim", columnList = "claim_id"),
                @Index(name = "idx_claim_history_time", columnList = "occurred_at")})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "claim_id", nullable = false)
    private UUID claimId;

    @Column(name = "event_type", length = 50, nullable = false)
    private String eventType;

    @Column(name = "previous_status", length = 30)
    private String previousStatus;

    @Column(name = "new_status", length = 30)
    private String newStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_data", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> eventData = Map.of();

    @Column(name = "occurred_at")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    @Column(name = "performed_by", nullable = false)
    private UUID performedBy;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;
}
