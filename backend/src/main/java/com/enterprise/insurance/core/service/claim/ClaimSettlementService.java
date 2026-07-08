package com.enterprise.insurance.core.service.claim;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.enterprise.insurance.core.domain.claim.Claim;
import com.enterprise.insurance.core.domain.claim.ClaimHistory;
import com.enterprise.insurance.core.domain.claim.ClaimStatus;
import com.enterprise.insurance.core.dto.claim.PaymentRequest;
import com.enterprise.insurance.core.exception.EntityNotFoundException;
import com.enterprise.insurance.core.repository.claim.ClaimHistoryRepository;
import com.enterprise.insurance.core.repository.claim.ClaimRepository;

@Service
@Transactional
public class ClaimSettlementService {

    private static final Logger log = LoggerFactory.getLogger(ClaimSettlementService.class);

    private final ClaimRepository claimRepository;
    private final ClaimHistoryRepository claimHistoryRepository;
    private final ClaimEventPublisher eventPublisher;

    public ClaimSettlementService(ClaimRepository claimRepository,
            ClaimHistoryRepository claimHistoryRepository, ClaimEventPublisher eventPublisher) {
        this.claimRepository = claimRepository;
        this.claimHistoryRepository = claimHistoryRepository;
        this.eventPublisher = eventPublisher;
    }

    public Claim processPayment(String claimNumber, PaymentRequest request) {
        Claim claim = getClaim(claimNumber);
        String previousStatus = claim.getStatus().name();

        if (!claim.getStatus().isPayable() && claim.getStatus() != ClaimStatus.PAYMENT_PENDING) {
            throw new IllegalStateException("Claim " + claimNumber
                    + " is not in a payable state. Current status: " + claim.getStatus());
        }

        BigDecimal outstanding = claim.calculateOutstandingAmount();
        if (request.getAmount().compareTo(outstanding) > 0) {
            throw new IllegalArgumentException("Payment amount " + request.getAmount()
                    + " exceeds outstanding amount " + outstanding);
        }

        claim.pay(request.getAmount(), request.getTransactionReference());
        claim = claimRepository.save(claim);

        saveHistory(claim, "PAYMENT_PROCESSED", previousStatus, ClaimStatus.PAID.name(),
                Map.of("amount", request.getAmount(), "paymentMethod", request.getPaymentMethod(),
                        "transactionReference", request.getTransactionReference(), "notes",
                        request.getNotes()),
                request.getPaidBy());

        eventPublisher.publishClaimPaid(claim, request.getTransactionReference(),
                request.getPaymentMethod(), request.getPaidBy());
        eventPublisher.publishClaimStatusChanged(claim, previousStatus, "Payment processed",
                request.getPaidBy());

        log.info("Payment of {} processed for claim: {}", request.getAmount(), claimNumber);
        return claim;
    }

    public BigDecimal calculatePaymentAmount(String claimNumber) {
        Claim claim = getClaim(claimNumber);
        return claim.calculateOutstandingAmount();
    }

    public Claim closeClaim(String claimNumber, String closedBy) {
        Claim claim = getClaim(claimNumber);
        String previousStatus = claim.getStatus().name();

        claim.close();
        claim = claimRepository.save(claim);

        saveHistory(claim, "CLAIM_CLOSED", previousStatus, ClaimStatus.CLOSED.name(),
                Map.of("settlementDays", claim.getSettlementDays()), closedBy);

        eventPublisher.publishClaimClosed(claim, closedBy);
        eventPublisher.publishClaimStatusChanged(claim, previousStatus, "Claim closed", closedBy);

        log.info("Claim {} closed. Settlement days: {}", claimNumber, claim.getSettlementDays());
        return claim;
    }

    public Claim reopenClaim(String claimNumber, String reason, String reopenedBy) {
        Claim claim = getClaim(claimNumber);
        String previousStatus = claim.getStatus().name();

        claim.reopen();
        claim = claimRepository.save(claim);

        saveHistory(claim, "CLAIM_REOPENED", previousStatus, ClaimStatus.REOPENED.name(),
                Map.of("reason", reason), reopenedBy);

        eventPublisher.publishClaimStatusChanged(claim, previousStatus, "Claim reopened: " + reason,
                reopenedBy);

        log.info("Claim {} reopened. Reason: {}", claimNumber, reason);
        return claim;
    }

    public String getClaimPaymentStatus(String claimNumber) {
        Claim claim = getClaim(claimNumber);
        return claim.getStatus().name();
    }

    private Claim getClaim(String claimNumber) {
        return claimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found: " + claimNumber));
    }

    private void saveHistory(Claim claim, String eventType, String previousStatus, String newStatus,
            Map<String, Object> eventData, String performedBy) {
        ClaimHistory history = ClaimHistory.builder().claimId(claim.getId()).eventType(eventType)
                .previousStatus(previousStatus).newStatus(newStatus).eventData(eventData)
                .performedBy(UUID.fromString(performedBy)).tenantId(claim.getTenantId()).build();
        claimHistoryRepository.save(history);
    }
}
