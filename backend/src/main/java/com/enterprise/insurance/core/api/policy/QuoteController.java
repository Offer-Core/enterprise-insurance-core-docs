package com.enterprise.insurance.core.api.policy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.enterprise.insurance.core.domain.Policy;
import com.enterprise.insurance.core.domain.quote.Quote;
import com.enterprise.insurance.core.domain.quote.QuoteStatus;
import com.enterprise.insurance.core.dto.quote.QuoteRequest;
import com.enterprise.insurance.core.dto.quote.QuoteResponse;
import com.enterprise.insurance.core.service.quote.QuoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/quotes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Quotes", description = "Quote management APIs")
public class QuoteController {

    private final QuoteService quoteService;

    @PostMapping
    @Operation(summary = "Create a new quote",
            description = "Creates a new insurance quote request")
    @ApiResponses(
            value = {@ApiResponse(responseCode = "201", description = "Quote created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request")})
    public ResponseEntity<QuoteResponse> createQuote(@Valid @RequestBody QuoteRequest request,
            @RequestParam(defaultValue = "default") String tenantId) {
        Quote quote = quoteService.createQuote(request, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(quote));
    }

    @PostMapping("/{quoteNumber}/submit")
    @Operation(summary = "Submit a quote for rating")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Quote submitted"),
            @ApiResponse(responseCode = "404", description = "Quote not found")})
    public ResponseEntity<QuoteResponse> submitQuote(
            @Parameter(description = "Quote number") @PathVariable String quoteNumber) {
        Quote quote = quoteService.submitQuote(quoteNumber);
        return ResponseEntity.ok(mapToResponse(quote));
    }

    @PostMapping("/{quoteNumber}/rate")
    @Operation(summary = "Rate a quote with calculated premium")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Quote rated"),
            @ApiResponse(responseCode = "404", description = "Quote not found")})
    public ResponseEntity<QuoteResponse> rateQuote(
            @Parameter(description = "Quote number") @PathVariable String quoteNumber,
            @RequestBody RateRequest request) {
        Quote quote = quoteService.rateQuote(quoteNumber, request.basePremium(),
                request.taxAmount(), request.finalPremium());
        return ResponseEntity.ok(mapToResponse(quote));
    }

    @PostMapping("/{quoteNumber}/accept")
    @Operation(summary = "Accept a quote", description = "Customer accepts the quoted premium")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Quote accepted"),
            @ApiResponse(responseCode = "400", description = "Quote expired"),
            @ApiResponse(responseCode = "404", description = "Quote not found")})
    public ResponseEntity<QuoteResponse> acceptQuote(
            @Parameter(description = "Quote number") @PathVariable String quoteNumber) {
        Quote quote = quoteService.acceptQuote(quoteNumber);
        return ResponseEntity.ok(mapToResponse(quote));
    }

    @PostMapping("/{quoteNumber}/reject")
    @Operation(summary = "Reject a quote", description = "Customer rejects the quote")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Quote rejected"),
            @ApiResponse(responseCode = "404", description = "Quote not found")})
    public ResponseEntity<QuoteResponse> rejectQuote(
            @Parameter(description = "Quote number") @PathVariable String quoteNumber,
            @RequestBody RejectRequest request) {
        Quote quote = quoteService.rejectQuote(quoteNumber, request.reason());
        return ResponseEntity.ok(mapToResponse(quote));
    }

    @PostMapping("/{quoteNumber}/bind")
    @Operation(summary = "Bind a quote to a policy",
            description = "Converts an accepted quote into a policy")
    @ApiResponses(
            value = {@ApiResponse(responseCode = "200", description = "Quote bound to policy"),
                    @ApiResponse(responseCode = "400", description = "Quote expired or invalid"),
                    @ApiResponse(responseCode = "404", description = "Quote not found")})
    public ResponseEntity<QuoteResponse> bindQuote(
            @Parameter(description = "Quote number") @PathVariable String quoteNumber,
            @RequestBody BindRequest request) {
        Policy policy = quoteService.bindQuote(quoteNumber, request.customerId(),
                request.effectiveDate(), request.tenantId());
        return ResponseEntity
                .ok(QuoteResponse.builder().quoteNumber(quoteNumber).status("BOUND").build());
    }

    @GetMapping("/{quoteNumber}")
    @Operation(summary = "Get quote by number")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Quote found"),
            @ApiResponse(responseCode = "404", description = "Quote not found")})
    public ResponseEntity<QuoteResponse> getQuote(
            @Parameter(description = "Quote number") @PathVariable String quoteNumber) {
        Quote quote = quoteService.findQuoteByNumber(quoteNumber);
        return ResponseEntity.ok(mapToResponse(quote));
    }

    @GetMapping
    @Operation(summary = "List quotes by status")
    public ResponseEntity<List<QuoteResponse>> getQuotesByStatus(
            @RequestParam(required = false) QuoteStatus status) {
        List<Quote> quotes = status != null ? quoteService.getQuotesByStatus(status)
                : quoteService.getQuotesByStatus(QuoteStatus.DRAFT);
        return ResponseEntity.ok(quotes.stream().map(this::mapToResponse).toList());
    }

    @PostMapping("/expire-stale")
    @Operation(summary = "Expire stale quotes",
            description = "Expires all quotes past their validity period")
    public ResponseEntity<Integer> expireStaleQuotes() {
        int count = quoteService.expireStaleQuotes();
        return ResponseEntity.ok(count);
    }

    private QuoteResponse mapToResponse(Quote quote) {
        return QuoteResponse.builder().quoteNumber(quote.getQuoteNumber())
                .productCode(quote.getProductCode()).basePremium(quote.getBasePremium())
                .totalPremium(quote.getFinalPremium()).taxAmount(quote.getTaxAmount())
                .currency(quote.getCurrency()).effectiveDate(quote.getEffectiveDate())
                .expiryDate(quote.getExpiryDate()).expiresAt(quote.getExpiresAt())
                .status(quote.getStatus().name()).build();
    }

    public record RateRequest(BigDecimal basePremium, BigDecimal taxAmount,
            BigDecimal finalPremium) {
    }
    public record RejectRequest(String reason) {
    }
    public record BindRequest(UUID customerId, java.time.LocalDate effectiveDate, String tenantId) {
    }
}
