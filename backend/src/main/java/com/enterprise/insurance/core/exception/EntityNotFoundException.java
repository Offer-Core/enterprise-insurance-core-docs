package com.enterprise.insurance.core.exception;

import java.util.UUID;

/**
 * Exception thrown when a requested entity is not found.
 */
public class EntityNotFoundException extends RuntimeException {

    private final String entityType;
    private final UUID entityId;

    public EntityNotFoundException(String entityType, UUID entityId) {
        super(entityType + " not found with id: " + entityId);
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public EntityNotFoundException(String entityType, String identifier) {
        super(entityType + " not found with identifier: " + identifier);
        this.entityType = entityType;
        this.entityId = null;
    }

    public EntityNotFoundException(String message) {
        super(message);
        this.entityType = null;
        this.entityId = null;
    }

    public String getEntityType() {
        return entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }
}
