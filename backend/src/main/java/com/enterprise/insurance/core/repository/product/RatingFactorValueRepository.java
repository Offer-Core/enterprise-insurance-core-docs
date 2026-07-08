package com.enterprise.insurance.core.repository.product;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.enterprise.insurance.core.domain.rating.RatingFactorValue;

@Repository
public interface RatingFactorValueRepository extends JpaRepository<RatingFactorValue, UUID> {

    List<RatingFactorValue> findByFactorIdOrderByPriority(UUID factorId);

    List<RatingFactorValue> findByFactorIdAndIsActiveTrueOrderByPriority(UUID factorId);

    void deleteByFactorId(UUID factorId);
}
