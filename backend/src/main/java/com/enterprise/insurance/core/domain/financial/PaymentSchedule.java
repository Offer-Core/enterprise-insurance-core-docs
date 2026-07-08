package com.enterprise.insurance.core.domain.financial;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a payment schedule for a policy, supporting installment plans.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSchedule {

    private String scheduleType; // ANNUAL, SEMI_ANNUAL, QUARTERLY, MONTHLY
    private Integer numberOfInstallments;
    private BigDecimal totalAmount;
    private BigDecimal installmentAmount;
    private String currency;
    private List<Installment> installments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Installment {
        private Integer installmentNumber;
        private LocalDate dueDate;
        private BigDecimal amount;
        private String status; // PENDING, PAID, OVERDUE, CANCELLED
        private LocalDate paidDate;
        private String paymentReference;
    }
}
