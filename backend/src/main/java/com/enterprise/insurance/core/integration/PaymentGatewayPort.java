package com.enterprise.insurance.core.integration;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Port interface for payment gateway integration (MADA, SADAD, etc.).
 */
public interface PaymentGatewayPort {

    /**
     * Process a payment.
     *
     * @param request payment request
     * @return payment result
     */
    PaymentResult processPayment(PaymentRequest request);

    /**
     * Check the status of a payment.
     *
     * @param transactionId gateway transaction ID
     * @return payment status
     */
    PaymentStatus checkPaymentStatus(String transactionId);

    /**
     * Process a refund.
     *
     * @param request refund request
     * @return refund result
     */
    RefundResult processRefund(RefundRequest request);

    /**
     * Check if the payment gateway is healthy.
     */
    boolean isHealthy();

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class PaymentRequest {
        private String transactionReference;
        private BigDecimal amount;
        private String currency;
        private String paymentMethod; // MADA, SADAD, APPLE_PAY, etc.
        private String customerName;
        private String customerMobile;
        private String customerEmail;
        private String description;
        private String callbackUrl;
        private Map<String, String> additionalData;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class PaymentResult {
        private boolean success;
        private String gatewayTransactionId;
        private String paymentUrl;
        private String status;
        private String authorizationCode;
        private String errorCode;
        private String errorMessage;
        private Map<String, Object> gatewayResponse;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class PaymentStatus {
        private String gatewayTransactionId;
        private String status; // PENDING, COMPLETED, FAILED, REFUNDED
        private BigDecimal amount;
        private String currency;
        private String authorizationCode;
        private String failureReason;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class RefundRequest {
        private String originalTransactionId;
        private BigDecimal amount;
        private String reason;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class RefundResult {
        private boolean success;
        private String refundTransactionId;
        private String status;
        private String errorMessage;
    }
}
