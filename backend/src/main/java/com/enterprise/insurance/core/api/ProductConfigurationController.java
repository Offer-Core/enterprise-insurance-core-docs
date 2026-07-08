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
import com.enterprise.insurance.core.domain.product.ProductConfiguration;
import com.enterprise.insurance.core.service.product.ProductConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@Tag(name = "Product Configuration",
        description = "Admin API for managing insurance product configurations")
public class ProductConfigurationController {

    private final ProductConfigurationService productService;

    @PostMapping
    @Operation(summary = "Create a new product configuration")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid product data"),
            @ApiResponse(responseCode = "409", description = "Product code already exists")})
    public ResponseEntity<ProductConfiguration> createProduct(
            @Valid @RequestBody ProductConfiguration product,
            @RequestHeader("X-User-Id") UUID userId) {
        productService.validateProduct(product);
        ProductConfiguration created = productService.createProduct(product, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{productCode}")
    @Operation(summary = "Update an existing product configuration")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")})
    public ResponseEntity<ProductConfiguration> updateProduct(@PathVariable String productCode,
            @Valid @RequestBody ProductConfiguration updates,
            @RequestHeader("X-User-Id") UUID userId) {
        ProductConfiguration updated = productService.updateProduct(productCode, updates, userId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    @Operation(summary = "Get all product configurations")
    @ApiResponse(responseCode = "200", description = "List of all products")
    public ResponseEntity<List<ProductConfiguration>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/active")
    @Operation(summary = "Get active product configurations")
    @ApiResponse(responseCode = "200", description = "List of active products")
    public ResponseEntity<List<ProductConfiguration>> getActiveProducts() {
        return ResponseEntity.ok(productService.getActiveProducts());
    }

    @GetMapping("/{productCode}")
    @Operation(summary = "Get product by code")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")})
    public ResponseEntity<ProductConfiguration> getProduct(@PathVariable String productCode) {
        return ResponseEntity.ok(productService.getProduct(productCode));
    }

    @GetMapping("/line-of-business/{lob}")
    @Operation(summary = "Get products by line of business")
    @ApiResponse(responseCode = "200", description = "List of products for the line of business")
    public ResponseEntity<List<ProductConfiguration>> getProductsByLineOfBusiness(
            @PathVariable String lob) {
        return ResponseEntity.ok(productService.getProductsByLineOfBusiness(lob));
    }

    @DeleteMapping("/{productCode}")
    @Operation(summary = "Deactivate a product")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Product deactivated"),
            @ApiResponse(responseCode = "404", description = "Product not found")})
    public ResponseEntity<Void> deactivateProduct(@PathVariable String productCode,
            @RequestHeader("X-User-Id") UUID userId) {
        productService.deleteProduct(productCode, userId);
        return ResponseEntity.noContent().build();
    }
}
