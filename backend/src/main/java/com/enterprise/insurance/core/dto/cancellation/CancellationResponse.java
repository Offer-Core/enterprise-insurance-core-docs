package com.enterprise.insurance.core.dto.cancellation;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancellationResponse {

    private String policyNumber;
    private String cancellationReason;
    private LocalDate cancellationDate;
    private BigDecimal refundAmount;
    private BigDecimal penaltyAmount;
    private String refundMethod;
    private String status;
    private String message;
}
