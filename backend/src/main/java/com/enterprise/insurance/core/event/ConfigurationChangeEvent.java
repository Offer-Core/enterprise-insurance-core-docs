package com.enterprise.insurance.core.event;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event representing a configuration change in the product/rating/underwriting system. All
 * configuration changes are event-sourced for audit and traceability.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigurationChangeEvent {

    private UUID eventId;
    private String eventType;
    private String aggregateType;
    private String aggregateId;
    private Integer version;
    private Object oldState;
    private Object newState;
    private String changedBy;
    private String changeReason;
    private String ipAddress;
    @Builder.Default
    private Instant occurredAt = Instant.now();

    /**
     * Predefined event types for configuration changes.
     */
    public static final class EventTypes {
        public static final String PRODUCT_CREATED = "PRODUCT_CREATED";
        public static final String PRODUCT_UPDATED = "PRODUCT_UPDATED";
        public static final String PRODUCT_DEACTIVATED = "PRODUCT_DEACTIVATED";
        public static final String BENEFIT_ADDED = "BENEFIT_ADDED";
        public static final String BENEFIT_UPDATED = "BENEFIT_UPDATED";
        public static final String BENEFIT_REMOVED = "BENEFIT_REMOVED";
        public static final String RATING_FACTOR_CREATED = "RATING_FACTOR_CREATED";
        public static final String RATING_FACTOR_UPDATED = "RATING_FACTOR_UPDATED";
        public static final String RATING_FACTOR_ENABLED = "RATING_FACTOR_ENABLED";
        public static final String RATING_FACTOR_DISABLED = "RATING_FACTOR_DISABLED";
        public static final String UNDERWRITING_RULE_CREATED = "UNDERWRITING_RULE_CREATED";
        public static final String UNDERWRITING_RULE_UPDATED = "UNDERWRITING_RULE_UPDATED";
        public static final String UNDERWRITING_RULE_DELETED = "UNDERWRITING_RULE_DELETED";
        public static final String BENEFIT_CATALOG_CREATED = "BENEFIT_CATALOG_CREATED";
        public static final String BENEFIT_CATALOG_UPDATED = "BENEFIT_CATALOG_UPDATED";

        private EventTypes() {}
    }

    /**
     * Predefined aggregate types.
     */
    public static final class AggregateTypes {
        public static final String PRODUCT = "PRODUCT";
        public static final String BENEFIT = "BENEFIT";
        public static final String RATING_FACTOR = "RATING_FACTOR";
        public static final String UNDERWRITING_RULE = "UNDERWRITING_RULE";
        public static final String BENEFIT_CATALOG = "BENEFIT_CATALOG";

        private AggregateTypes() {}
    }
}
