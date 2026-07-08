package com.enterprise.insurance.core.domain.claim;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "claim_workflows", schema = "metadata")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimWorkflowDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "workflow_code", length = 50, unique = true, nullable = false)
    private String workflowCode;

    @Column(name = "workflow_name_ar", length = 200, nullable = false)
    private String workflowNameAr;

    @Column(name = "workflow_name_en", length = 200, nullable = false)
    private String workflowNameEn;

    @Column(name = "line_of_business", length = 30, nullable = false)
    private String lineOfBusiness;

    @Column(name = "claim_type", length = 50, nullable = false)
    private String claimType;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<WorkflowState> states;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<WorkflowTransition> transitions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> configuration = Map.of();

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class WorkflowState {
        private String stateCode;
        private String stateNameAr;
        private String stateNameEn;
        private String description;
        private List<String> allowedActions;
        private Boolean isTerminal;
        private Integer slaDays;
        private Integer displayOrder;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class WorkflowTransition {
        private String fromState;
        private String toState;
        private String action;
        private String condition;
        private Boolean requiresApproval;
        private String approvalRole;
        private String notificationTemplate;
    }
}
