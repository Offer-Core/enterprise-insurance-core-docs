package com.enterprise.insurance.core.repository.endorsement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.enterprise.insurance.core.domain.endorsement.Endorsement;
import com.enterprise.insurance.core.domain.endorsement.EndorsementStatus;

@Repository
public interface EndorsementRepository extends JpaRepository<Endorsement, UUID> {

    Optional<Endorsement> findByEndorsementNumber(String endorsementNumber);

    List<Endorsement> findByPolicyId(UUID policyId);

    List<Endorsement> findByStatus(EndorsementStatus status);

    long countByPolicyId(UUID policyId);
}
