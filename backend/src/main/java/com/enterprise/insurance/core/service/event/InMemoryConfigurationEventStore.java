package com.enterprise.insurance.core.service.event;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import com.enterprise.insurance.core.event.ConfigurationChangeEvent;

/**
 * In-memory implementation of ConfigurationEventStore for development/testing. In production, this
 * would be backed by the event_store.metadata_events table.
 */
@Repository
public class InMemoryConfigurationEventStore implements ConfigurationEventStore {

    private final List<ConfigurationChangeEvent> events = new CopyOnWriteArrayList<>();
    private final Map<String, List<ConfigurationChangeEvent>> aggregateIndex =
            new ConcurrentHashMap<>();

    @Override
    public void append(ConfigurationChangeEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID());
        }
        if (event.getOccurredAt() == null) {
            event.setOccurredAt(Instant.now());
        }
        events.add(event);
        aggregateIndex.computeIfAbsent(event.getAggregateType() + ":" + event.getAggregateId(),
                k -> new CopyOnWriteArrayList<>()).add(event);
    }

    @Override
    public List<ConfigurationChangeEvent> readEvents(String aggregateType, String aggregateId) {
        return aggregateIndex
                .getOrDefault(aggregateType + ":" + aggregateId, Collections.emptyList()).stream()
                .sorted(Comparator.comparing(ConfigurationChangeEvent::getOccurredAt))
                .collect(Collectors.toList());
    }

    @Override
    public ConfigurationChangeEvent getLatestVersion(String aggregateType, String aggregateId) {
        return aggregateIndex
                .getOrDefault(aggregateType + ":" + aggregateId, Collections.emptyList()).stream()
                .max(Comparator.comparing(ConfigurationChangeEvent::getOccurredAt)).orElse(null);
    }

    @Override
    public List<ConfigurationChangeEvent> readEventsByUser(String userId) {
        return events.stream().filter(e -> userId.equals(e.getChangedBy()))
                .sorted(Comparator.comparing(ConfigurationChangeEvent::getOccurredAt))
                .collect(Collectors.toList());
    }

    @Override
    public List<ConfigurationChangeEvent> readEventsByDateRange(Instant from, Instant to) {
        return events.stream()
                .filter(e -> !e.getOccurredAt().isBefore(from) && !e.getOccurredAt().isAfter(to))
                .sorted(Comparator.comparing(ConfigurationChangeEvent::getOccurredAt))
                .collect(Collectors.toList());
    }

    @Override
    public List<ConfigurationChangeEvent> readEventsByType(String eventType) {
        return events.stream().filter(e -> eventType.equals(e.getEventType()))
                .sorted(Comparator.comparing(ConfigurationChangeEvent::getOccurredAt))
                .collect(Collectors.toList());
    }
}
