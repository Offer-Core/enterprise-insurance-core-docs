package com.enterprise.insurance.core.dto.endorsement;

import java.time.LocalDate;
import java.util.Map;
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
public class EndorsementRequest {

    @NotBlank
    private String policyNumber;

    @NotBlank
    private String endorsementType;

    @NotBlank
    private String description;

    @NotNull
    private LocalDate effectiveDate;

    @NotNull
    private Map<String, Object> changes;
}
