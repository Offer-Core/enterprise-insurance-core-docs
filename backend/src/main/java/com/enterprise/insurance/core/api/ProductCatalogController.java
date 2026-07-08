package com.enterprise.insurance.core.api;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.enterprise.insurance.core.service.product.ProductCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
@Tag(name = "Product Catalog", description = "Public API for browsing the product catalog")
public class ProductCatalogController {

    private final ProductCatalogService catalogService;

    @GetMapping
    @Operation(summary = "Get the full product catalog with benefits")
    @ApiResponse(responseCode = "200", description = "Product catalog retrieved successfully")
    public ResponseEntity<Map<String, Object>> getCatalog() {
        return ResponseEntity.ok(catalogService.getProductCatalog());
    }

    @GetMapping("/{productCode}")
    @Operation(summary = "Get detailed product information")
    @ApiResponse(responseCode = "200", description = "Product details retrieved successfully")
    public ResponseEntity<Map<String, Object>> getProductDetails(@PathVariable String productCode) {
        return ResponseEntity.ok(catalogService.getProductDetails(productCode));
    }

    @PostMapping("/compare")
    @Operation(summary = "Compare multiple products")
    @ApiResponse(responseCode = "200", description = "Product comparison retrieved successfully")
    public ResponseEntity<List<Map<String, Object>>> compareProducts(
            @RequestBody List<String> productCodes) {
        return ResponseEntity.ok(catalogService.getProductComparison(productCodes));
    }
}
