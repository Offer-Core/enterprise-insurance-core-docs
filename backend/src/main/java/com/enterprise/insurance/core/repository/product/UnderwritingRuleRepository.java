package com.enterprise.insurance.core.repository.product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.enterprise.insurance.core.domain.underwriting.UnderwritingRuleEntity;

@Repository
public interface UnderwritingRuleRepository extends JpaRepository<UnderwritingRuleEntity, UUID> {

    Optional<UnderwritingRuleEntity> findByRuleCode(String ruleCode);

    List<UnderwritingRuleEntity> findByIsActiveTrueOrderByDisplayOrder();

    List<UnderwritingRuleEntity> findByRuleType(String ruleType);

    List<UnderwritingRuleEntity> findByAction(String action);

    boolean existsByRuleCode(String ruleCode);
}
