package com.enterprise.insurance.core.dto.claim;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponse {
    private String claimNumber;
    private String policyNumber;
    private String customerId;
    private String customerName;
    private String status;
    private String claimType;
    private String lineOfBusiness;
    private LocalDate incidentDate;
    private String incidentLocation;
    private BigDecimal claimedAmount;
    private BigDecimal approvedAmount;
    private BigDecimal paidAmount;
    private BigDecimal fraudScore;
    private String handlerName;
    private String adjusterName;
    private LocalDateTime reportedDate;
    private LocalDateTime closedAt;
    private Integer settlementDays;
    private String currentWorkflowState;
    private List<String> availableActions;
    private Map<String, Object> details;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
