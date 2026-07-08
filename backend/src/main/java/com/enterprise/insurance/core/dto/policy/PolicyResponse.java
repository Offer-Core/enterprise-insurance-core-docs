package com.enterprise.insurance.core.dto.policy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyResponse {

    private String policyNumber;
    private String productCode;
    private String productNameAr;
    private String productNameEn;
    private UUID customerId;
    private String customerName;
    private String status;
    private BigDecimal annualPremium;
    private BigDecimal taxAmount;
    private BigDecimal totalPremium;
    private String currency;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    private String policyDocumentUrl;
    private LocalDateTime issuedAt;
    private LocalDateTime activatedAt;
    private LocalDateTime createdAt;
    private Integer renewalCount;
    private Map<String, Object> additionalDetails;
}
