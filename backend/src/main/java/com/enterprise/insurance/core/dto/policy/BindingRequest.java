package com.enterprise.insurance.core.dto.policy;

import java.time.LocalDate;
import java.util.UUID;
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
public class BindingRequest {

    @NotBlank
    private String quoteNumber;

    @NotNull
    private UUID customerId;

    @NotNull
    private LocalDate effectiveDate;

    @NotBlank
    private String paymentMethod;

    private String paymentToken;

    @NotNull
    private Boolean acceptTerms;

    private UUID agentId;

    private String tenantId;
}
