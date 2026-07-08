package com.enterprise.insurance.lines.motor.api;

import com.enterprise.insurance.lines.motor.application.MotorPolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/v1/motor/policies")
@RequiredArgsConstructor
public class MotorPolicyController {

    private final MotorPolicyService policyService;

    @PostMapping
    public ResponseEntity<MotorPolicyResponse> createQuote(@Valid @RequestBody MotorQuoteRequest request) {
        MotorPolicyResponse response = policyService.createQuote(request);
        return ResponseEntity
                .created(URI.create("/v1/motor/policies/" + response.getPolicyId()))
                .body(response);
    }

    @GetMapping("/{policyId}")
    public ResponseEntity<MotorPolicyResponse> getPolicy(@PathVariable UUID policyId) {
        // TODO: Implement getPolicy
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
