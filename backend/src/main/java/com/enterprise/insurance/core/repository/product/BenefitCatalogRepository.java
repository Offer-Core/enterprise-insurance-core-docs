package com.enterprise.insurance.core.repository.product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.enterprise.insurance.core.domain.product.BenefitCatalog;

@Repository
public interface BenefitCatalogRepository extends JpaRepository<BenefitCatalog, UUID> {

    Optional<BenefitCatalog> findByBenefitCode(String benefitCode);

    List<BenefitCatalog> findByIsActiveTrue();

    List<BenefitCatalog> findByBenefitCategory(String benefitCategory);

    boolean existsByBenefitCode(String benefitCode);
}
