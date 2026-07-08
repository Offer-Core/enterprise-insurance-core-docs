package com.enterprise.insurance.core.repository.claim;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.enterprise.insurance.core.domain.claim.FraudRule;

@Repository
public interface FraudRuleRepository extends JpaRepository<FraudRule, UUID> {

    Optional<FraudRule> findByRuleCode(String ruleCode);

    List<FraudRule> findByIsActiveTrue();

    List<FraudRule> findBySeverityAndIsActiveTrue(String severity);

    List<FraudRule> findByRuleTypeAndIsActiveTrue(String ruleType);
}
