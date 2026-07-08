package com.enterprise.insurance.core.controller.claim;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.enterprise.insurance.core.domain.claim.ClaimDocument;
import com.enterprise.insurance.core.service.claim.ClaimDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/claims/{claimNumber}/documents")
@Tag(name = "Claim Documents", description = "Claim document management API")
public class ClaimDocumentController {

    private final ClaimDocumentService documentService;

    public ClaimDocumentController(ClaimDocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    @Operation(summary = "Upload document", description = "Uploads a document to a claim")
    public ResponseEntity<ClaimDocument> uploadDocument(@PathVariable String claimNumber,
            @RequestParam String documentType, @RequestParam String documentName,
            @RequestParam String documentUrl, @RequestParam(required = false) Long fileSize,
            @RequestParam(required = false) String mimeType, @RequestParam String uploadedBy,
            @RequestParam String tenantId) {
        ClaimDocument document = documentService.uploadDocument(claimNumber, documentType,
                documentName, documentUrl, fileSize, mimeType, uploadedBy, tenantId);
        return ResponseEntity.ok(document);
    }

    @GetMapping
    @Operation(summary = "Get documents", description = "Retrieves all documents for a claim")
    public ResponseEntity<List<ClaimDocument>> getDocuments(@PathVariable String claimNumber) {
        List<ClaimDocument> documents = documentService.getDocuments(claimNumber);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{documentId}")
    @Operation(summary = "Get document", description = "Retrieves a specific document by ID")
    public ResponseEntity<ClaimDocument> getDocument(@PathVariable UUID documentId) {
        ClaimDocument document = documentService.getClaimDocument(documentId);
        return ResponseEntity.ok(document);
    }

    @PostMapping("/generate-report")
    @Operation(summary = "Generate claim report", description = "Generates a PDF claim report")
    public ResponseEntity<String> generateClaimReport(@PathVariable String claimNumber) {
        String reportUrl = documentService.generateClaimReport(claimNumber);
        return ResponseEntity.ok(reportUrl);
    }

    @PostMapping("/generate-settlement")
    @Operation(summary = "Generate settlement statement",
            description = "Generates a PDF settlement statement")
    public ResponseEntity<String> generateSettlementStatement(@PathVariable String claimNumber) {
        String statementUrl = documentService.generateSettlementStatement(claimNumber);
        return ResponseEntity.ok(statementUrl);
    }
}
