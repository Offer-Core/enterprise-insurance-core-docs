package com.enterprise.insurance.core.dto.underwriting;

import java.math.BigDecimal;
import java.util.UUID;
import com.enterprise.insurance.core.domain.underwriting.UnderwritingAction;
import com.enterprise.insurance.core.domain.underwriting.UnderwritingRuleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnderwritingRuleDTO {

    private UUID id;

    @NotBlank(message = "Rule code is required")
    private String ruleCode;

    @NotBlank(message = "Arabic rule name is required")
    private String ruleNameAr;

    @NotBlank(message = "English rule name is required")
    private String ruleNameEn;

    @NotNull(message = "Rule type is required")
    private UnderwritingRuleType ruleType;

    private String severity;

    @NotBlank(message = "Condition is required")
    private String condition;

    @NotNull(message = "Action is required")
    private UnderwritingAction action;

    private BigDecimal surchargeAmount;
    private String referralReason;
    private String rejectionReason;
    private Boolean isActive;
    private Integer displayOrder;
}
