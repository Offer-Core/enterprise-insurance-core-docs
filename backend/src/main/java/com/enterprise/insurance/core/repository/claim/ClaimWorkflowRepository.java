package com.enterprise.insurance.core.repository.claim;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.enterprise.insurance.core.domain.claim.ClaimWorkflowDefinition;

@Repository
public interface ClaimWorkflowRepository extends JpaRepository<ClaimWorkflowDefinition, UUID> {

    Optional<ClaimWorkflowDefinition> findByWorkflowCode(String workflowCode);

    List<ClaimWorkflowDefinition> findByLineOfBusinessAndIsActiveTrue(String lineOfBusiness);

    Optional<ClaimWorkflowDefinition> findByLineOfBusinessAndClaimTypeAndIsDefaultTrue(
            String lineOfBusiness, String claimType);

    List<ClaimWorkflowDefinition> findByClaimTypeAndIsActiveTrue(String claimType);
}
