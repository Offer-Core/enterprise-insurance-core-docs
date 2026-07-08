package com.enterprise.insurance.core.repository.claim;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.enterprise.insurance.core.domain.claim.ClaimAssignment;

@Repository
public interface ClaimAssignmentRepository extends JpaRepository<ClaimAssignment, UUID> {

    List<ClaimAssignment> findByClaimId(UUID claimId);

    Optional<ClaimAssignment> findByClaimIdAndIsActiveTrue(UUID claimId);

    List<ClaimAssignment> findByAssigneeIdAndIsActiveTrue(UUID assigneeId);

    List<ClaimAssignment> findByAssignmentTypeAndIsActiveTrue(String assignmentType);
}
