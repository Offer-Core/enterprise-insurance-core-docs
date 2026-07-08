package com.enterprise.insurance.core.api;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.enterprise.insurance.core.domain.product.ProductBenefit;
import com.enterprise.insurance.core.service.product.ProductBenefitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/products/{productId}/benefits")
@RequiredArgsConstructor
@Tag(name = "Product Benefits", description = "Admin API for managing product benefits")
public class ProductBenefitController {

    private final ProductBenefitService benefitService;

    @PostMapping
    @Operation(summary = "Add a benefit to a product")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Benefit added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid benefit data")})
    public ResponseEntity<ProductBenefit> addBenefit(@PathVariable UUID productId,
            @Valid @RequestBody ProductBenefit benefit, @RequestHeader("X-User-Id") UUID userId) {
        benefit.setProductId(productId);
        ProductBenefit created = benefitService.addBenefit(benefit, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{benefitId}")
    @Operation(summary = "Update a product benefit")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Benefit updated successfully"),
            @ApiResponse(responseCode = "404", description = "Benefit not found")})
    public ResponseEntity<ProductBenefit> updateBenefit(@PathVariable UUID productId,
            @PathVariable UUID benefitId, @Valid @RequestBody ProductBenefit updates,
            @RequestHeader("X-User-Id") UUID userId) {
        ProductBenefit updated = benefitService.updateBenefit(benefitId, updates, userId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    @Operation(summary = "Get all benefits for a product")
    @ApiResponse(responseCode = "200", description = "List of benefits")
    public ResponseEntity<List<ProductBenefit>> getBenefits(@PathVariable UUID productId) {
        return ResponseEntity.ok(benefitService.getBenefitsByProduct(productId));
    }

    @GetMapping("/default")
    @Operation(summary = "Get default benefits for a product")
    @ApiResponse(responseCode = "200", description = "List of default benefits")
    public ResponseEntity<List<ProductBenefit>> getDefaultBenefits(@PathVariable UUID productId) {
        return ResponseEntity.ok(benefitService.getDefaultBenefits(productId));
    }

    @GetMapping("/mandatory")
    @Operation(summary = "Get mandatory benefits for a product")
    @ApiResponse(responseCode = "200", description = "List of mandatory benefits")
    public ResponseEntity<List<ProductBenefit>> getMandatoryBenefits(@PathVariable UUID productId) {
        return ResponseEntity.ok(benefitService.getMandatoryBenefits(productId));
    }

    @GetMapping("/{benefitCode}")
    @Operation(summary = "Get a specific benefit by code")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Benefit found"),
            @ApiResponse(responseCode = "404", description = "Benefit not found")})
    public ResponseEntity<ProductBenefit> getBenefit(@PathVariable UUID productId,
            @PathVariable String benefitCode) {
        return ResponseEntity.ok(benefitService.getBenefit(productId, benefitCode));
    }

    @DeleteMapping("/{benefitId}")
    @Operation(summary = "Remove a benefit from a product")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Benefit removed"),
            @ApiResponse(responseCode = "404", description = "Benefit not found")})
    public ResponseEntity<Void> removeBenefit(@PathVariable UUID productId,
            @PathVariable UUID benefitId, @RequestHeader("X-User-Id") UUID userId) {
        benefitService.removeBenefit(benefitId, userId);
        return ResponseEntity.noContent().build();
    }
}
