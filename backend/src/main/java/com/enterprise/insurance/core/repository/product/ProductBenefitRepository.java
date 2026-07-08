package com.enterprise.insurance.core.repository.product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.enterprise.insurance.core.domain.product.ProductBenefit;

@Repository
public interface ProductBenefitRepository extends JpaRepository<ProductBenefit, UUID> {

    List<ProductBenefit> findByProductIdOrderByDisplayOrder(UUID productId);

    List<ProductBenefit> findByProductIdAndIsActiveTrueOrderByDisplayOrder(UUID productId);

    List<ProductBenefit> findByProductIdAndIsOptionalFalse(UUID productId);

    List<ProductBenefit> findByProductIdAndIsIncludedByDefaultTrue(UUID productId);

    Optional<ProductBenefit> findByProductIdAndBenefitCode(UUID productId, String benefitCode);

    void deleteByProductId(UUID productId);
}
