package com.enterprise.insurance.core.repository.claim;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.enterprise.insurance.core.domain.claim.ClaimHistory;

@Repository
public interface ClaimHistoryRepository extends JpaRepository<ClaimHistory, UUID> {

    List<ClaimHistory> findByClaimIdOrderByOccurredAtAsc(UUID claimId);

    List<ClaimHistory> findByClaimIdAndOccurredAtBetweenOrderByOccurredAtAsc(UUID claimId,
            LocalDateTime from, LocalDateTime to);

    List<ClaimHistory> findByEventType(String eventType);
}
