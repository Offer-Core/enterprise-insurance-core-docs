package com.enterprise.insurance.core.dto.product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import com.enterprise.insurance.core.domain.product.ProductConfig;
import com.enterprise.insurance.core.domain.product.ProductType;
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
public class ProductConfigurationDTO {

    private UUID id;

    @NotBlank(message = "Product code is required")
    private String productCode;

    @NotBlank(message = "Arabic product name is required")
    private String productNameAr;

    @NotBlank(message = "English product name is required")
    private String productNameEn;

    @NotNull(message = "Product type is required")
    private ProductType productType;

    @NotBlank(message = "Line of business is required")
    private String lineOfBusiness;

    private String productDescriptionAr;
    private String productDescriptionEn;
    private Boolean isActive;
    private BigDecimal minimumPremium;
    private BigDecimal maximumPremium;
    private ProductConfig config;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
    private Integer version;
    private UUID createdBy;
}
