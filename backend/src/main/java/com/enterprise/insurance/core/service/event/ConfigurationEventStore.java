package com.enterprise.insurance.core.service.event;

import java.time.Instant;
import java.util.List;
import com.enterprise.insurance.core.event.ConfigurationChangeEvent;

/**
 * Event store interface for persisting and retrieving configuration change events. All product,
 * benefit, rating factor, and underwriting rule changes are event-sourced.
 */
public interface ConfigurationEventStore {

    /**
     * Append a configuration change event to the event store.
     */
    void append(ConfigurationChangeEvent event);

    /**
     * Read all events for a specific aggregate (e.g., a product or rule).
     */
    List<ConfigurationChangeEvent> readEvents(String aggregateType, String aggregateId);

    /**
     * Get the latest version of an aggregate.
     */
    ConfigurationChangeEvent getLatestVersion(String aggregateType, String aggregateId);

    /**
     * Read all events created by a specific user.
     */
    List<ConfigurationChangeEvent> readEventsByUser(String userId);

    /**
     * Read events within a date range.
     */
    List<ConfigurationChangeEvent> readEventsByDateRange(Instant from, Instant to);

    /**
     * Read all events of a specific type.
     */
    List<ConfigurationChangeEvent> readEventsByType(String eventType);
}
