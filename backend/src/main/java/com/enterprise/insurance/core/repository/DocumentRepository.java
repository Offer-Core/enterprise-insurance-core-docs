package com.enterprise.insurance.core.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.enterprise.insurance.core.domain.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    Optional<Document> findByDocumentReference(String documentReference);

    List<Document> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    List<Document> findByDocumentType(String documentType);

    List<Document> findByTenantId(String tenantId);

    List<Document> findByStatus(String status);

    List<Document> findByEntityTypeAndEntityIdAndDocumentType(String entityType, UUID entityId,
            String documentType);
}
