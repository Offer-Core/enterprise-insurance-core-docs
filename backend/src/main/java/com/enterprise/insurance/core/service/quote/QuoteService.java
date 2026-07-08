package com.enterprise.insurance.core.service.quote;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.enterprise.insurance.core.domain.Policy;
import com.enterprise.insurance.core.domain.PolicyStatus;
import com.enterprise.insurance.core.domain.quote.Quote;
import com.enterprise.insurance.core.domain.quote.QuoteStatus;
import com.enterprise.insurance.core.dto.quote.QuoteRequest;
import com.enterprise.insurance.core.exception.BusinessRuleException;
import com.enterprise.insurance.core.exception.EntityNotFoundException;
import com.enterprise.insurance.core.repository.PolicyRepository;
import com.enterprise.insurance.core.repository.quote.QuoteRepository;
import com.enterprise.insurance.core.service.policy.PolicyLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final PolicyRepository policyRepository;
    private final PolicyLifecycleService policyLifecycleService;

    /**
     * Creates a new quote request.
     */
    @Transactional
    public Quote createQuote(QuoteRequest request, String tenantId) {
        Quote quote = Quote.builder().quoteNumber(generateQuoteNumber())
                .customerId(request.getCustomerId()).productCode(request.getProductCode())
                .effectiveDate(request.getEffectiveDate())
                .expiryDate(request.getEffectiveDate().plusYears(1)).basePremium(BigDecimal.ZERO)
                .finalPremium(BigDecimal.ZERO).status(QuoteStatus.DRAFT).tenantId(tenantId).build();

        Quote saved = quoteRepository.save(quote);
        log.info("Quote created: {} for customer: {}", saved.getQuoteNumber(),
                request.getCustomerId());
        return saved;
    }

    /**
     * Submits a quote for rating.
     */
    @Transactional
    public Quote submitQuote(String quoteNumber) {
        Quote quote = findQuoteByNumber(quoteNumber);
        quote.transitionTo(QuoteStatus.SUBMITTED);
        Quote saved = quoteRepository.save(quote);
        log.info("Quote submitted: {}", saved.getQuoteNumber());
        return saved;
    }

    /**
     * Rates a quote with calculated premium.
     */
    @Transactional
    public Quote rateQuote(String quoteNumber, BigDecimal basePremium, BigDecimal taxAmount,
            BigDecimal finalPremium) {
        Quote quote = findQuoteByNumber(quoteNumber);
        quote.transitionTo(QuoteStatus.RATED);
        quote.setBasePremium(basePremium);
        quote.setFinalPremium(finalPremium);
        quote.setTaxAmount(taxAmount);
        Quote saved = quoteRepository.save(quote);
        log.info("Quote rated: {} premium: {}", saved.getQuoteNumber(), finalPremium);
        return saved;
    }

    /**
     * Customer accepts the quote.
     */
    @Transactional
    public Quote acceptQuote(String quoteNumber) {
        Quote quote = findQuoteByNumber(quoteNumber);

        if (quote.isExpired()) {
            quote.transitionTo(QuoteStatus.EXPIRED);
            quoteRepository.save(quote);
            throw new BusinessRuleException("QUOTE_EXPIRED",
                    "Quote " + quoteNumber + " has expired");
        }

        quote.transitionTo(QuoteStatus.ACCEPTED);
        Quote saved = quoteRepository.save(quote);
        log.info("Quote accepted: {}", saved.getQuoteNumber());
        return saved;
    }

    /**
     * Customer rejects the quote.
     */
    @Transactional
    public Quote rejectQuote(String quoteNumber, String reason) {
        Quote quote = findQuoteByNumber(quoteNumber);
        quote.transitionTo(QuoteStatus.REJECTED);
        quote.setRejectedReason(reason);
        Quote saved = quoteRepository.save(quote);
        log.info("Quote rejected: {} reason: {}", saved.getQuoteNumber(), reason);
        return saved;
    }

    /**
     * Binds a quote to a policy.
     */
    @Transactional
    public Policy bindQuote(String quoteNumber, UUID customerId, LocalDate effectiveDate,
            String tenantId) {
        Quote quote = findQuoteByNumber(quoteNumber);

        if (quote.isExpired()) {
            quote.transitionTo(QuoteStatus.EXPIRED);
            quoteRepository.save(quote);
            throw new BusinessRuleException("QUOTE_EXPIRED",
                    "Quote " + quoteNumber + " has expired");
        }

        quote.transitionTo(QuoteStatus.BOUND);
        quoteRepository.save(quote);

        // Create policy from quote
        Policy policy = Policy.builder().policyNumber(generatePolicyNumber())
                .productCode(quote.getProductCode()).customer(findCustomerReference(customerId))
                .status(PolicyStatus.BOUND).effectiveDate(effectiveDate)
                .expiryDate(effectiveDate.plusYears(1)).annualPremium(quote.getFinalPremium())
                .taxAmount(quote.getTaxAmount())
                .totalPremium(quote.getFinalPremium().add(quote.getTaxAmount())).currency("SAR")
                .tenantId(tenantId).build();

        Policy savedPolicy = policyLifecycleService.createPolicy(policy);

        // Link quote to policy
        quote.setPolicyId(savedPolicy.getId());
        quoteRepository.save(quote);

        log.info("Quote {} bound to policy: {}", quoteNumber, savedPolicy.getPolicyNumber());
        return savedPolicy;
    }

    /**
     * Expires stale quotes.
     */
    @Transactional
    public int expireStaleQuotes() {
        List<QuoteStatus> activeStatuses = List.of(QuoteStatus.DRAFT, QuoteStatus.SUBMITTED,
                QuoteStatus.RATED, QuoteStatus.ACCEPTED);

        List<Quote> expiredQuotes =
                quoteRepository.findExpiredQuotes(LocalDateTime.now(), activeStatuses);

        for (Quote quote : expiredQuotes) {
            quote.transitionTo(QuoteStatus.EXPIRED);
        }
        quoteRepository.saveAll(expiredQuotes);

        log.info("Expired {} stale quotes", expiredQuotes.size());
        return expiredQuotes.size();
    }

    public Quote findQuoteByNumber(String quoteNumber) {
        return quoteRepository.findByQuoteNumber(quoteNumber)
                .orElseThrow(() -> new EntityNotFoundException("Quote", quoteNumber));
    }

    public List<Quote> getQuotesByCustomer(UUID customerId) {
        return quoteRepository.findByCustomerId(customerId);
    }

    public List<Quote> getQuotesByStatus(QuoteStatus status) {
        return quoteRepository.findByStatus(status);
    }

    private String generateQuoteNumber() {
        return "QTE-" + System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String generatePolicyNumber() {
        return "POL-" + System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    // Placeholder - will be replaced with CustomerRepository lookup
    private com.enterprise.insurance.core.domain.Customer findCustomerReference(UUID customerId) {
        return policyRepository.findById(customerId).map(p -> p.getCustomer()).orElse(null);
    }
}
