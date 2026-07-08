package com.enterprise.insurance.core.dto.product;

import java.math.BigDecimal;
import java.util.UUID;
import com.enterprise.insurance.core.domain.product.BenefitCatalog.BenefitCategory;
import com.enterprise.insurance.core.domain.product.BenefitCatalog.CalculationMethod;
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
public class BenefitCatalogDTO {

    private UUID id;

    @NotBlank(message = "Benefit code is required")
    private String benefitCode;

    @NotBlank(message = "Arabic benefit name is required")
    private String benefitNameAr;

    @NotBlank(message = "English benefit name is required")
    private String benefitNameEn;

    private String benefitDescriptionAr;
    private String benefitDescriptionEn;

    @NotNull(message = "Benefit category is required")
    private BenefitCategory benefitCategory;

    private BigDecimal defaultPrice;
    private BigDecimal maximumPrice;
    private CalculationMethod calculationMethod;
    private String calculationParams;
    private Boolean isActive;
    private String applicableProductTypes;
}
