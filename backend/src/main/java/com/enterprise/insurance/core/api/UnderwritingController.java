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
import com.enterprise.insurance.core.domain.underwriting.UnderwritingRuleEntity;
import com.enterprise.insurance.core.dto.underwriting.UnderwritingEvaluationRequest;
import com.enterprise.insurance.core.dto.underwriting.UnderwritingEvaluationResponse;
import com.enterprise.insurance.core.service.underwriting.UnderwritingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/underwriting")
@RequiredArgsConstructor
@Tag(name = "Underwriting Rules",
        description = "Admin API for managing underwriting rules and evaluation")
public class UnderwritingController {

    private final UnderwritingService underwritingService;

    @PostMapping("/evaluate")
    @Operation(summary = "Evaluate underwriting rules for a quote")
    @ApiResponse(responseCode = "200", description = "Rules evaluated successfully")
    public ResponseEntity<UnderwritingEvaluationResponse> evaluateRules(
            @Valid @RequestBody UnderwritingEvaluationRequest request) {
        return ResponseEntity.ok(underwritingService.evaluateRules(request));
    }

    @GetMapping("/rules")
    @Operation(summary = "Get all active underwriting rules")
    @ApiResponse(responseCode = "200", description = "List of active rules")
    public ResponseEntity<List<UnderwritingRuleEntity>> getRules() {
        return ResponseEntity.ok(underwritingService.getRules());
    }

    @GetMapping("/rules/all")
    @Operation(summary = "Get all underwriting rules (including inactive)")
    @ApiResponse(responseCode = "200", description = "List of all rules")
    public ResponseEntity<List<UnderwritingRuleEntity>> getAllRules() {
        return ResponseEntity.ok(underwritingService.getAllRules());
    }

    @GetMapping("/rules/{ruleCode}")
    @Operation(summary = "Get an underwriting rule by code")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Rule found"),
            @ApiResponse(responseCode = "404", description = "Rule not found")})
    public ResponseEntity<UnderwritingRuleEntity> getRule(@PathVariable String ruleCode) {
        return ResponseEntity.ok(underwritingService.getRule(ruleCode));
    }

    @PostMapping("/rules")
    @Operation(summary = "Create a new underwriting rule")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Rule created"),
            @ApiResponse(responseCode = "400", description = "Invalid rule data"),
            @ApiResponse(responseCode = "409", description = "Rule code already exists")})
    public ResponseEntity<UnderwritingRuleEntity> addRule(
            @Valid @RequestBody UnderwritingRuleEntity rule,
            @RequestHeader("X-User-Id") UUID userId) {
        UnderwritingRuleEntity created = underwritingService.addRule(rule, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/rules/{ruleCode}")
    @Operation(summary = "Update an underwriting rule")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Rule updated"),
            @ApiResponse(responseCode = "404", description = "Rule not found")})
    public ResponseEntity<UnderwritingRuleEntity> updateRule(@PathVariable String ruleCode,
            @Valid @RequestBody UnderwritingRuleEntity updates,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(underwritingService.updateRule(ruleCode, updates, userId));
    }

    @DeleteMapping("/rules/{ruleCode}")
    @Operation(summary = "Delete an underwriting rule")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Rule deleted"),
            @ApiResponse(responseCode = "404", description = "Rule not found")})
    public ResponseEntity<Void> deleteRule(@PathVariable String ruleCode,
            @RequestHeader("X-User-Id") UUID userId) {
        underwritingService.deleteRule(ruleCode, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/rules/{ruleCode}/enable")
    @Operation(summary = "Enable an underwriting rule")
    @ApiResponse(responseCode = "200", description = "Rule enabled")
    public ResponseEntity<Void> enableRule(@PathVariable String ruleCode,
            @RequestHeader("X-User-Id") UUID userId) {
        underwritingService.enableRule(ruleCode, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rules/{ruleCode}/disable")
    @Operation(summary = "Disable an underwriting rule")
    @ApiResponse(responseCode = "200", description = "Rule disabled")
    public ResponseEntity<Void> disableRule(@PathVariable String ruleCode,
            @RequestHeader("X-User-Id") UUID userId) {
        underwritingService.disableRule(ruleCode, userId);
        return ResponseEntity.ok().build();
    }
}
