package com.enterprise.insurance.core.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "documents", schema = "core",
        indexes = {@Index(name = "idx_documents_entity", columnList = "entity_type, entity_id"),
                @Index(name = "idx_documents_tenant", columnList = "tenant_id"),
                @Index(name = "idx_documents_type", columnList = "document_type")})
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Document extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "document_reference", length = 50, unique = true, nullable = false)
    private String documentReference;

    @Column(name = "document_type", length = 30, nullable = false)
    private String documentType;

    @Column(name = "entity_type", length = 30, nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "document_name_ar", length = 200)
    private String documentNameAr;

    @Column(name = "document_name_en", length = 200)
    private String documentNameEn;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(length = 64)
    private String checksum;

    @Column(nullable = false)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @Column(name = "signature_reference", length = 100)
    private String signatureReference;

    @Column(name = "document_version")
    @Builder.Default
    private Integer documentVersion = 1;

    @Column(name = "is_template")
    @Builder.Default
    private Boolean isTemplate = false;

    @Column(name = "template_code", length = 50)
    private String templateCode;

    @Column(length = 500)
    private String description;

    @Column(name = "language", length = 10)
    @Builder.Default
    private String language = "ar";
}
