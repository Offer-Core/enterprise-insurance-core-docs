package com.enterprise.insurance.core.dto.endorsement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndorsementResponse {

    private String endorsementNumber;
    private String policyNumber;
    private String endorsementType;
    private String description;
    private LocalDate effectiveDate;
    private BigDecimal premiumAdjustment;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private String approvedBy;
}
