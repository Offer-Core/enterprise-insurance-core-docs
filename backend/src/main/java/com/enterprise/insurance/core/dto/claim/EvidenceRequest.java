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
public class EvidenceRequest {
    @NotBlank(message = "Evidence type is required")
    private String evidenceType;

    @NotBlank(message = "Description is required")
    private String description;

    private String documentUrl;

    private String notes;

    @NotBlank(message = "Uploaded by is required")
    private String uploadedBy;
}
