package com.enterprise.insurance.core.dto.cancellation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancellationRequest {

    @NotBlank
    private String policyNumber;

    @NotBlank
    private String cancellationReason;

    @NotBlank
    private String cancellationCode;

    @NotNull
    private Boolean proRataRefund;

    private String requestedBy;
}
