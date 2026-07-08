package com.enterprise.insurance.core.repository.claim;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.enterprise.insurance.core.domain.claim.VehicleDamage;

@Repository
public interface VehicleDamageRepository extends JpaRepository<VehicleDamage, UUID> {

    List<VehicleDamage> findByClaimId(UUID claimId);
}
