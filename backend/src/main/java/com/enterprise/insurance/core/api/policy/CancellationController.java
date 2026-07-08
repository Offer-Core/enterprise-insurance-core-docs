package com.enterprise.insurance.core.api.policy;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.enterprise.insurance.core.dto.cancellation.CancellationRequest;
import com.enterprise.insurance.core.dto.cancellation.CancellationResponse;
import com.enterprise.insurance.core.service.cancellation.CancellationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/cancellations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cancellations", description = "Policy cancellation APIs")
public class CancellationController {

    private final CancellationService cancellationService;

    @PostMapping
    @Operation(summary = "Cancel a policy",
            description = "Processes policy cancellation with refund calculation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Policy cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Policy cannot be cancelled"),
            @ApiResponse(responseCode = "404", description = "Policy not found")})
    public ResponseEntity<CancellationResponse> cancelPolicy(
            @Valid @RequestBody CancellationRequest request) {
        CancellationResponse response = cancellationService.cancelPolicy(request);
        return ResponseEntity.ok(response);
    }
}
