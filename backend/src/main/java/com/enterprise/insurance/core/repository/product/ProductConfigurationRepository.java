package com.enterprise.insurance.core.repository.product;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.enterprise.insurance.core.domain.product.ProductConfiguration;

@Repository
public interface ProductConfigurationRepository extends JpaRepository<ProductConfiguration, UUID> {

    Optional<ProductConfiguration> findByProductCode(String productCode);

    List<ProductConfiguration> findByIsActiveTrue();

    List<ProductConfiguration> findByLineOfBusiness(String lineOfBusiness);

    List<ProductConfiguration> findByProductType(String productType);

    @Query("SELECT p FROM ProductConfiguration p WHERE p.isActive = true "
            + "AND p.effectiveFrom <= :today AND (p.effectiveTo IS NULL OR p.effectiveTo >= :today)")
    List<ProductConfiguration> findActiveProducts(@Param("today") LocalDate today);

    @Query("SELECT p FROM ProductConfiguration p WHERE p.lineOfBusiness = :lob "
            + "AND p.isActive = true AND p.effectiveFrom <= :today "
            + "AND (p.effectiveTo IS NULL OR p.effectiveTo >= :today)")
    List<ProductConfiguration> findActiveByLineOfBusiness(@Param("lob") String lob,
            @Param("today") LocalDate today);

    @Query("SELECT p FROM ProductConfiguration p WHERE p.productType = :type "
            + "AND p.isActive = true AND p.effectiveFrom <= :today "
            + "AND (p.effectiveTo IS NULL OR p.effectiveTo >= :today)")
    List<ProductConfiguration> findActiveByProductType(@Param("type") String type,
            @Param("today") LocalDate today);

    boolean existsByProductCode(String productCode);
}
