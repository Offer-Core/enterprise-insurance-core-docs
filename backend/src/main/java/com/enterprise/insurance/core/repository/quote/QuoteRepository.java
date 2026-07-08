package com.enterprise.insurance.core.repository.quote;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.enterprise.insurance.core.domain.quote.Quote;
import com.enterprise.insurance.core.domain.quote.QuoteStatus;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, UUID> {

    Optional<Quote> findByQuoteNumber(String quoteNumber);

    List<Quote> findByCustomerId(UUID customerId);

    List<Quote> findByStatus(QuoteStatus status);

    List<Quote> findByProductCode(String productCode);

    @Query("SELECT q FROM Quote q WHERE q.status IN :statuses")
    List<Quote> findByStatusIn(@Param("statuses") List<QuoteStatus> statuses);

    @Query("SELECT q FROM Quote q WHERE q.expiresAt < :now AND q.status IN :activeStatuses")
    List<Quote> findExpiredQuotes(@Param("now") LocalDateTime now,
            @Param("activeStatuses") List<QuoteStatus> activeStatuses);

    @Query("SELECT q FROM Quote q WHERE q.customerId = :customerId AND q.status = 'RATED'")
    List<Quote> findActiveQuotesByCustomer(@Param("customerId") UUID customerId);

    @Query("SELECT q FROM Quote q WHERE q.referenceId = :referenceId")
    Optional<Quote> findByReferenceId(@Param("referenceId") String referenceId);

    long countByStatus(QuoteStatus status);

    @Query("SELECT q FROM Quote q WHERE q.createdAt BETWEEN :from AND :to")
    List<Quote> findByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
