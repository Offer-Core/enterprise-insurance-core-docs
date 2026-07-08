package com.enterprise.insurance.core.api;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.enterprise.insurance.core.domain.product.BenefitCatalog;
import com.enterprise.insurance.core.service.product.ProductCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/benefit-catalog")
@RequiredArgsConstructor
@Tag(name = "Benefit Catalog", description = "Admin API for managing the global benefit catalog")
public class BenefitCatalogController {

    private final ProductCatalogService catalogService;

    @PostMapping
    @Operation(summary = "Create a new benefit catalog entry")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Benefit catalog entry created"),
            @ApiResponse(responseCode = "400", description = "Invalid benefit data"),
            @ApiResponse(responseCode = "409", description = "Benefit code already exists")})
    public ResponseEntity<BenefitCatalog> createBenefit(@Valid @RequestBody BenefitCatalog benefit,
            @RequestHeader("X-User-Id") UUID userId) {
        BenefitCatalog created = catalogService.createBenefitCatalog(benefit, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{benefitCode}")
    @Operation(summary = "Update a benefit catalog entry")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Benefit catalog entry updated"),
            @ApiResponse(responseCode = "404", description = "Benefit not found")})
    public ResponseEntity<BenefitCatalog> updateBenefit(@PathVariable String benefitCode,
            @Valid @RequestBody BenefitCatalog updates, @RequestHeader("X-User-Id") UUID userId) {
        BenefitCatalog updated = catalogService.updateBenefitCatalog(benefitCode, updates, userId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    @Operation(summary = "Get all active benefit catalog entries")
    @ApiResponse(responseCode = "200", description = "List of active benefits")
    public ResponseEntity<List<BenefitCatalog>> getActiveBenefits() {
        return ResponseEntity.ok(catalogService.getActiveBenefits());
    }

    @GetMapping("/{benefitCode}")
    @Operation(summary = "Get a benefit catalog entry by code")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Benefit found"),
            @ApiResponse(responseCode = "404", description = "Benefit not found")})
    public ResponseEntity<BenefitCatalog> getBenefit(@PathVariable String benefitCode) {
        return ResponseEntity.ok(catalogService.getBenefitByCode(benefitCode));
    }
}
