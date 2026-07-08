package com.enterprise.insurance.core.repository.claim;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.enterprise.insurance.core.domain.claim.ClaimDocument;

@Repository
public interface ClaimDocumentRepository extends JpaRepository<ClaimDocument, UUID> {

    List<ClaimDocument> findByClaimId(UUID claimId);

    List<ClaimDocument> findByClaimIdAndDocumentType(UUID claimId, String documentType);
}
