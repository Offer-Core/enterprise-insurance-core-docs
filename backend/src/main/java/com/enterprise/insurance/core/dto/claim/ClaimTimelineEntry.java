package com.enterprise.insurance.core.dto.claim;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimTimelineEntry {
    private LocalDateTime timestamp;
    private String eventType;
    private String description;
    private String previousStatus;
    private String newStatus;
    private String performedBy;
    private Map<String, Object> details;
}
