package com.enterprise.insurance.core.api;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.enterprise.insurance.core.domain.rating.RatingFactorEntity;
import com.enterprise.insurance.core.domain.rating.RatingFactorValue;
import com.enterprise.insurance.core.dto.rating.PremiumCalculationRequest;
import com.enterprise.insurance.core.dto.rating.PremiumCalculationResponse;
import com.enterprise.insurance.core.service.rating.RatingEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/rating-factors")
@RequiredArgsConstructor
@Tag(name = "Rating Factors", description = "Admin API for managing configurable rating factors")
public class RatingFactorController {

    private final RatingEngineService ratingEngineService;

    @GetMapping
    @Operation(summary = "Get all active rating factors")
    @ApiResponse(responseCode = "200", description = "List of active rating factors")
    public ResponseEntity<List<RatingFactorEntity>> getRatingFactors() {
        return ResponseEntity.ok(ratingEngineService.getRatingFactors());
    }

    @GetMapping("/all")
    @Operation(summary = "Get all rating factors (including inactive)")
    @ApiResponse(responseCode = "200", description = "List of all rating factors")
    public ResponseEntity<List<RatingFactorEntity>> getAllRatingFactors() {
        return ResponseEntity.ok(ratingEngineService.getAllRatingFactors());
    }

    @GetMapping("/{factorCode}")
    @Operation(summary = "Get a rating factor by code")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Rating factor found"),
            @ApiResponse(responseCode = "404", description = "Rating factor not found")})
    public ResponseEntity<RatingFactorEntity> getRatingFactor(@PathVariable String factorCode) {
        return ResponseEntity.ok(ratingEngineService.getRatingFactor(factorCode));
    }

    @PutMapping("/{factorCode}")
    @Operation(summary = "Update a rating factor")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Rating factor updated"),
            @ApiResponse(responseCode = "404", description = "Rating factor not found")})
    public ResponseEntity<RatingFactorEntity> updateRatingFactor(@PathVariable String factorCode,
            @Valid @RequestBody RatingFactorEntity updates,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity
                .ok(ratingEngineService.updateRatingFactor(factorCode, updates, userId));
    }

    @PostMapping("/{factorCode}/enable")
    @Operation(summary = "Enable a rating factor")
    @ApiResponse(responseCode = "200", description = "Rating factor enabled")
    public ResponseEntity<Void> enableFactor(@PathVariable String factorCode,
            @RequestHeader("X-User-Id") UUID userId) {
        ratingEngineService.enableFactor(factorCode, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{factorCode}/disable")
    @Operation(summary = "Disable a rating factor")
    @ApiResponse(responseCode = "200", description = "Rating factor disabled")
    public ResponseEntity<Void> disableFactor(@PathVariable String factorCode,
            @RequestHeader("X-User-Id") UUID userId) {
        ratingEngineService.disableFactor(factorCode, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{factorId}/values")
    @Operation(summary = "Get values for a rating factor")
    @ApiResponse(responseCode = "200", description = "List of factor values")
    public ResponseEntity<List<RatingFactorValue>> getFactorValues(@PathVariable UUID factorId) {
        return ResponseEntity.ok(ratingEngineService.getFactorValues(factorId));
    }

    @PostMapping("/{factorId}/values")
    @Operation(summary = "Add a value to a rating factor")
    @ApiResponse(responseCode = "200", description = "Factor value added")
    public ResponseEntity<RatingFactorValue> addFactorValue(@PathVariable UUID factorId,
            @Valid @RequestBody RatingFactorValue value) {
        value.setFactorId(factorId);
        return ResponseEntity.ok(ratingEngineService.addFactorValue(value));
    }

    @PostMapping("/calculate")
    @Operation(summary = "Calculate premium using rating engine")
    @ApiResponse(responseCode = "200", description = "Premium calculated successfully")
    public ResponseEntity<PremiumCalculationResponse> calculatePremium(
            @Valid @RequestBody PremiumCalculationRequest request) {
        return ResponseEntity.ok(ratingEngineService.calculatePremium(request));
    }
}
