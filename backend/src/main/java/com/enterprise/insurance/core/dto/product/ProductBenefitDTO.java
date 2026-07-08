package com.enterprise.insurance.core.dto.product;

import java.math.BigDecimal;
import java.util.UUID;
import com.enterprise.insurance.core.domain.product.BenefitType;
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
public class ProductBenefitDTO {

    private UUID id;
    private UUID productId;

    @NotBlank(message = "Benefit code is required")
    private String benefitCode;

    @NotBlank(message = "Arabic benefit name is required")
    private String benefitNameAr;

    @NotBlank(message = "English benefit name is required")
    private String benefitNameEn;

    private String benefitDescriptionAr;
    private String benefitDescriptionEn;

    @NotNull(message = "Benefit type is required")
    private BenefitType benefitType;

    private BigDecimal defaultPrice;
    private BigDecimal priceOverride;
    private Boolean isOptional;
    private Boolean isIncludedByDefault;
    private Boolean isActive;
    private String conditions;
    private Integer displayOrder;
}
