package com.enterprise.insurance.core.dto.claim;

import java.time.LocalDate;
import com.enterprise.insurance.core.domain.claim.ClaimStatus;
import com.enterprise.insurance.core.domain.claim.ClaimType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimSearchRequest {
    private String claimNumber;
    private String policyNumber;
    private String customerId;
    private ClaimStatus status;
    private ClaimType claimType;
    private String lineOfBusiness;
    private LocalDate incidentDateFrom;
    private LocalDate incidentDateTo;
    private LocalDate reportedDateFrom;
    private LocalDate reportedDateTo;
    private String handlerId;
    private Boolean fraudReviewRequired;
    private String tenantId;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;
}
