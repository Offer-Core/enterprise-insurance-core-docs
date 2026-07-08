package com.enterprise.insurance.core.dto.claim;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectionRequest {
    @NotBlank(message = "Rejection reason is required")
    private String reason;

    private String rejectionCode;

    private String notes;

    @NotBlank(message = "Rejected by is required")
    private String rejectedBy;
}
