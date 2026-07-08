package com.enterprise.insurance.core.domain.claim;

import java.time.LocalDateTime;
import java.util.UUID;
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
@Table(name = "claim_assignments", schema = "core",
        indexes = {@Index(name = "idx_claim_assignments_claim", columnList = "claim_id"),
                @Index(name = "idx_claim_assignments_active", columnList = "claim_id",
                        unique = false)})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "claim_id", nullable = false)
    private UUID claimId;

    @Column(name = "assignee_id", nullable = false)
    private UUID assigneeId;

    @Column(name = "assignee_name", length = 200)
    private String assigneeName;

    @Column(name = "assignee_role", length = 50)
    private String assigneeRole;

    @Column(name = "assignment_type", length = 30)
    private String assignmentType;

    @Column(name = "assigned_at")
    @Builder.Default
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "assigned_by")
    private UUID assignedBy;

    @Column(name = "deassigned_at")
    private LocalDateTime deassignedAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;
}
