package com.enterprise.insurance.core.dto.claim;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest {
    @NotNull(message = "Approved amount is required")
    @Positive(message = "Approved amount must be positive")
    private BigDecimal approvedAmount;

    private String reason;

    private List<String> conditions;

    private String notes;

    @NotNull(message = "Approved by is required")
    private String approvedBy;
}
