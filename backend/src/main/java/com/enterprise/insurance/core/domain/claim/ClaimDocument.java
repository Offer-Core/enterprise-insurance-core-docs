package com.enterprise.insurance.core.domain.claim;

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
import lombok.NoArgsConstructor;

@Entity
@Table(name = "claim_documents", schema = "core",
        indexes = {@Index(name = "idx_claim_documents_claim", columnList = "claim_id")})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "claim_id", nullable = false)
    private UUID claimId;

    @Column(name = "document_type", length = 50, nullable = false)
    private String documentType;

    @Column(name = "document_name", length = 255, nullable = false)
    private String documentName;

    @Column(name = "document_url", columnDefinition = "TEXT", nullable = false)
    private String documentUrl;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(length = 64)
    private String checksum;

    @Column(name = "uploaded_at")
    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @Column(name = "uploaded_by", nullable = false)
    private UUID uploadedBy;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;
}
