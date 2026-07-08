package com.enterprise.insurance.core.dto.rating;

import java.util.UUID;
import com.enterprise.insurance.core.domain.rating.RatingFactorType;
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
public class RatingFactorDTO {

    private UUID id;

    @NotBlank(message = "Factor code is required")
    private String factorCode;

    @NotBlank(message = "Arabic factor name is required")
    private String factorNameAr;

    @NotBlank(message = "English factor name is required")
    private String factorNameEn;

    @NotNull(message = "Factor type is required")
    private RatingFactorType factorType;

    private String calculationMethod;
    private String parameters;
    private Boolean isActive;
    private Integer displayOrder;
}
