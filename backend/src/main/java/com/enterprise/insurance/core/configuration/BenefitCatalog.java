package com.enterprise.insurance.core.configuration;

import java.util.List;
import java.util.Optional;
import com.enterprise.insurance.core.domain.benefits.Benefit;
import com.enterprise.insurance.core.domain.benefits.ProductBenefit;

/**
 * Service interface for managing the benefit catalog.
 */
public interface BenefitCatalog {

    Optional<Benefit> getBenefit(String benefitCode);

    List<Benefit> getActiveBenefits();

    List<Benefit> getBenefitsByLineOfBusiness(String lineOfBusiness);

    Benefit createBenefit(Benefit benefit);

    Benefit updateBenefit(String benefitCode, Benefit benefit);

    void deactivateBenefit(String benefitCode);

    List<ProductBenefit> getProductBenefits(String productCode);

    ProductBenefit assignBenefitToProduct(ProductBenefit productBenefit);

    void removeBenefitFromProduct(String productCode, String benefitCode);
}
