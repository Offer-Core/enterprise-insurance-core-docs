package com.enterprise.insurance.core.service.claim;

import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.enterprise.insurance.core.domain.claim.Claim;
import com.enterprise.insurance.core.domain.claim.ClaimDocument;
import com.enterprise.insurance.core.exception.EntityNotFoundException;
import com.enterprise.insurance.core.repository.claim.ClaimDocumentRepository;
import com.enterprise.insurance.core.repository.claim.ClaimRepository;

@Service
@Transactional
public class ClaimDocumentService {

    private static final Logger log = LoggerFactory.getLogger(ClaimDocumentService.class);

    private final ClaimRepository claimRepository;
    private final ClaimDocumentRepository documentRepository;

    public ClaimDocumentService(ClaimRepository claimRepository,
            ClaimDocumentRepository documentRepository) {
        this.claimRepository = claimRepository;
        this.documentRepository = documentRepository;
    }

    public ClaimDocument uploadDocument(String claimNumber, String documentType,
            String documentName, String documentUrl, Long fileSize, String mimeType,
            String uploadedBy, String tenantId) {
        Claim claim = getClaim(claimNumber);

        ClaimDocument document = ClaimDocument.builder().claimId(claim.getId())
                .documentType(documentType).documentName(documentName).documentUrl(documentUrl)
                .fileSize(fileSize).mimeType(mimeType).uploadedBy(UUID.fromString(uploadedBy))
                .tenantId(tenantId).build();

        document = documentRepository.save(document);
        log.info("Document {} uploaded for claim: {}", documentType, claimNumber);
        return document;
    }

    public List<ClaimDocument> getDocuments(String claimNumber) {
        Claim claim = getClaim(claimNumber);
        return documentRepository.findByClaimId(claim.getId());
    }

    public ClaimDocument getClaimDocument(UUID documentId) {
        return documentRepository.findById(documentId).orElseThrow(
                () -> new EntityNotFoundException("Document not found: " + documentId));
    }

    public String generateClaimReport(String claimNumber) {
        Claim claim = getClaim(claimNumber);
        // In production, this would generate a PDF report
        String reportUrl = "/reports/claims/" + claimNumber + "/report.pdf";
        log.info("Claim report generated for: {} at {}", claimNumber, reportUrl);
        return reportUrl;
    }

    public String generateSettlementStatement(String claimNumber) {
        Claim claim = getClaim(claimNumber);
        // In production, this would generate a PDF settlement statement
        String statementUrl = "/reports/claims/" + claimNumber + "/settlement.pdf";
        log.info("Settlement statement generated for: {} at {}", claimNumber, statementUrl);
        return statementUrl;
    }

    private Claim getClaim(String claimNumber) {
        return claimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found: " + claimNumber));
    }
}
