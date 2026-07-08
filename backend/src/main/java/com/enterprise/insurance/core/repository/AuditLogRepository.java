package com.enterprise.insurance.core.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.enterprise.insurance.core.domain.AuditLog;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByEntityTypeAndEntityIdOrderByOccurredAtDesc(String entityType,
            UUID entityId);

    List<AuditLog> findByAction(String action);

    List<AuditLog> findByChangedBy(UUID changedBy);

    List<AuditLog> findByTenantId(String tenantId);

    List<AuditLog> findByOccurredAtBetween(LocalDateTime start, LocalDateTime end);

    List<AuditLog> findByEntityTypeAndEntityIdAndAction(String entityType, UUID entityId,
            String action);
}
