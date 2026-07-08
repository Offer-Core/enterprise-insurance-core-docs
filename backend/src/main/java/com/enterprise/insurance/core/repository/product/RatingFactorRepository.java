package com.enterprise.insurance.core.repository.product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.enterprise.insurance.core.domain.rating.RatingFactorEntity;

@Repository
public interface RatingFactorRepository extends JpaRepository<RatingFactorEntity, UUID> {

    Optional<RatingFactorEntity> findByFactorCode(String factorCode);

    List<RatingFactorEntity> findByIsActiveTrueOrderByDisplayOrder();

    List<RatingFactorEntity> findByFactorType(String factorType);

    boolean existsByFactorCode(String factorCode);
}
