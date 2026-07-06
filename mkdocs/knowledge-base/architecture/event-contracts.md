# Event Contracts — Domain Event Schemas

## Overview

This document defines the complete event catalog for the Enterprise Insurance Platform. All domain events follow a standard envelope and are published to Apache Kafka topics. Each event represents a business fact that has occurred and is immutable once published.

---

## 1. Event Envelope

Every event follows this standard envelope structure:

```json
{
  "eventId": "evt_a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "eventType": "PolicyIssued",
  "eventVersion": "1.0",
  "sourceService": "policy-service",
  "sourceEntityType": "POLICY",
  "sourceEntityId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "occurredAt": "2026-07-06T10:30:00.000Z",
  "correlationId": "corr_abc123",
  "causationId": "evt_prev-event-id",
  "traceId": "trace_xyz789",
  "tenantId": "tenant-001",
  "data": { }
}
```

### Envelope Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `eventId` | `UUID` | Yes | Unique event identifier |
| `eventType` | `String` | Yes | PascalCase event name (e.g., `PolicyIssued`) |
| `eventVersion` | `String` | Yes | Semantic version of the event schema |
| `sourceService` | `String` | Yes | Service that produced the event |
| `sourceEntityType` | `String` | Yes | Entity type (POLICY, CLAIM, CUSTOMER, etc.) |
| `sourceEntityId` | `UUID` | Yes | ID of the entity that changed |
| `occurredAt` | `DateTime` | Yes | When the event occurred (UTC) |
| `correlationId` | `String` | Yes | Links events across a business transaction |
| `causationId` | `UUID` | No | ID of the event that caused this one |
| `traceId` | `String` | Yes | Distributed tracing ID |
| `tenantId` | `String` | Yes | Multi-tenant identifier |
| `data` | `Object` | Yes | Event-specific payload |

---

## 2. Event Catalog

### 2.1 Policy Events

#### PolicyIssued (v1.0)

**Producer:** Policy Service  
**Consumers:** Billing Service, Notification Service, Audit  
**Trigger:** Policy binding confirmed after payment

```json
{
  "eventType": "PolicyIssued",
  "eventVersion": "1.0",
  "sourceService": "policy-service",
  "data": {
    "policyId": "a1b2c3d4-...",
    "policyNumber": "POL-MTR-2026-00001",
    "productCode": "MOTOR_COMP",
    "lineOfBusiness": "MOTOR",
    "customerId": "b2c3d4e5-...",
    "agentId": "c3d4e5f6-...",
    "annualPremium": 2500.00,
    "currency": "SAR",
    "taxAmount": 375.00,
    "effectiveDate": "2026-01-01",
    "expiryDate": "2026-12-31",
    "coverageType": "COMPREHENSIVE",
    "vehicleInfo": {
      "sequenceNumber": "1234567890",
      "plateNumber": "ABC 1234",
      "make": "Toyota",
      "model": "Corolla",
      "modelYear": 2021
    }
  }
}
```

#### PolicyCancelled (v1.0)

**Producer:** Policy Service  
**Consumers:** Billing Service, Notification Service, Audit  
**Trigger:** Policy cancellation approved

```json
{
  "eventType": "PolicyCancelled",
  "eventVersion": "1.0",
  "sourceService": "policy-service",
  "data": {
    "policyId": "a1b2c3d4-...",
    "policyNumber": "POL-MTR-2026-00001",
    "cancellationReason": "CUSTOMER_REQUEST",
    "cancellationDate": "2026-06-15",
    "refundAmount": 1250.00,
    "currency": "SAR",
    "effectiveDate": "2026-01-01",
    "originalExpiryDate": "2026-12-31"
  }
}
```

#### PolicyRenewed (v1.0)

**Producer:** Policy Service  
**Consumers:** Billing Service, Notification Service, Audit  
**Trigger:** Policy renewal processed

```json
{
  "eventType": "PolicyRenewed",
  "eventVersion": "1.0",
  "sourceService": "policy-service",
  "data": {
    "oldPolicyId": "a1b2c3d4-...",
    "oldPolicyNumber": "POL-MTR-2026-00001",
    "newPolicyId": "d4e5f6a7-...",
    "newPolicyNumber": "POL-MTR-2027-00001",
    "customerId": "b2c3d4e5-...",
    "annualPremium": 2600.00,
    "currency": "SAR",
    "effectiveDate": "2027-01-01",
    "expiryDate": "2027-12-31",
    "premiumChange": 100.00,
    "premiumChangeReason": "NCD_IMPROVED"
  }
}
```

#### PolicyEndorsed (v1.0)

**Producer:** Policy Service  
**Consumers:** Billing Service, Audit  
**Trigger:** Mid-term policy change (e.g., vehicle change, driver addition)

```json
{
  "eventType": "PolicyEndorsed",
  "eventVersion": "1.0",
  "sourceService": "policy-service",
  "data": {
    "policyId": "a1b2c3d4-...",
    "policyNumber": "POL-MTR-2026-00001",
    "endorsementType": "VEHICLE_CHANGE",
    "endorsementNumber": "END-2026-00001",
    "premiumAdjustment": 200.00,
    "currency": "SAR",
    "effectiveDate": "2026-03-01",
    "previousState": {},
    "newState": {}
  }
}
```

---

### 2.2 Claim Events

#### ClaimRegistered (v1.0)

**Producer:** Claims Service  
**Consumers:** Notification Service, Audit, Policy Service  
**Trigger:** New claim filed (FNOL)

```json
{
  "eventType": "ClaimRegistered",
  "eventVersion": "1.0",
  "sourceService": "claims-service",
  "data": {
    "claimId": "e5f6a7b8-...",
    "claimNumber": "CLM-MTR-2026-00001",
    "policyId": "a1b2c3d4-...",
    "policyNumber": "POL-MTR-2026-00001",
    "customerId": "b2c3d4e5-...",
    "incidentDate": "2026-03-15",
    "incidentType": "COLLISION",
    "claimedAmount": 15000.00,
    "currency": "SAR",
    "lossLocation": "Riyadh, King Fahd Road",
    "policeReportNumber": "PR-2026-12345",
    "atFault": false
  }
}
```

#### ClaimUpdated (v1.0)

**Producer:** Claims Service  
**Consumers:** Notification Service, Audit  
**Trigger:** Claim status or details changed

```json
{
  "eventType": "ClaimUpdated",
  "eventVersion": "1.0",
  "sourceService": "claims-service",
  "data": {
    "claimId": "e5f6a7b8-...",
    "claimNumber": "CLM-MTR-2026-00001",
    "previousStatus": "INVESTIGATING",
    "newStatus": "ADJUDICATED",
    "approvedAmount": 12000.00,
    "currency": "SAR",
    "handlerId": "f6a7b8c9-...",
    "updatedFields": ["status", "approved_amount"]
  }
}
```

#### ClaimClosed (v1.0)

**Producer:** Claims Service  
**Consumers:** Billing Service, Notification Service, Audit  
**Trigger:** Claim fully settled and closed

```json
{
  "eventType": "ClaimClosed",
  "eventVersion": "1.0",
  "sourceService": "claims-service",
  "data": {
    "claimId": "e5f6a7b8-...",
    "claimNumber": "CLM-MTR-2026-00001",
    "policyId": "a1b2c3d4-...",
    "customerId": "b2c3d4e5-...",
    "claimedAmount": 15000.00,
    "approvedAmount": 12000.00,
    "paidAmount": 12000.00,
    "excessAmount": 500.00,
    "currency": "SAR",
    "closedAt": "2026-04-20T14:30:00.000Z",
    "settlementDays": 36,
    "fraudScore": 0.15
  }
}
```

#### FraudAlertTriggered (v1.0)

**Producer:** Claims Service  
**Consumers:** Audit, Notification Service  
**Trigger:** Fraud detection system flags a claim

```json
{
  "eventType": "FraudAlertTriggered",
  "eventVersion": "1.0",
  "sourceService": "claims-service",
  "data": {
    "claimId": "e5f6a7b8-...",
    "claimNumber": "CLM-MTR-2026-00001",
    "fraudScore": 0.85,
    "fraudIndicators": [
      "CLAIM_FILED_AFTER_POLICY_ISSUE_30_DAYS",
      "INCIDENT_TIME_INCONSISTENT",
      "MULTIPLE_CLAIMS_SAME_VEHICLE"
    ],
    "policyId": "a1b2c3d4-...",
    "customerId": "b2c3d4e5-..."
  }
}
```

---

### 2.3 Billing Events

#### PaymentReceived (v1.0)

**Producer:** Billing Service  
**Consumers:** Policy Service, Notification Service, Audit  
**Trigger:** Payment confirmed by gateway

```json
{
  "eventType": "PaymentReceived",
  "eventVersion": "1.0",
  "sourceService": "billing-service",
  "data": {
    "transactionId": "g7h8i9j0-...",
    "transactionReference": "TXN-2026-00001",
    "policyId": "a1b2c3d4-...",
    "policyNumber": "POL-MTR-2026-00001",
    "amount": 2500.00,
    "currency": "SAR",
    "paymentMethod": "MADA",
    "gatewayReference": "GW-987654321",
    "status": "COMPLETED",
    "processedAt": "2026-01-01T09:15:00.000Z"
  }
}
```

#### PaymentFailed (v1.0)

**Producer:** Billing Service  
**Consumers:** Notification Service, Audit  
**Trigger:** Payment declined or failed

```json
{
  "eventType": "PaymentFailed",
  "eventVersion": "1.0",
  "sourceService": "billing-service",
  "data": {
    "transactionId": "g7h8i9j0-...",
    "transactionReference": "TXN-2026-00001",
    "policyId": "a1b2c3d4-...",
    "policyNumber": "POL-MTR-2026-00001",
    "amount": 2500.00,
    "currency": "SAR",
    "paymentMethod": "MADA",
    "failureReason": "INSUFFICIENT_FUNDS",
    "gatewayErrorCode": "DECLINED_51",
    "retryCount": 2,
    "failedAt": "2026-01-01T09:15:00.000Z"
  }
}
```

#### RefundProcessed (v1.0)

**Producer:** Billing Service  
**Consumers:** Policy Service, Notification Service, Audit  
**Trigger:** Refund issued for cancelled policy or claim overpayment

```json
{
  "eventType": "RefundProcessed",
  "eventVersion": "1.0",
  "sourceService": "billing-service",
  "data": {
    "refundId": "h8i9j0k1-...",
    "originalTransactionId": "g7h8i9j0-...",
    "policyId": "a1b2c3d4-...",
    "policyNumber": "POL-MTR-2026-00001",
    "refundAmount": 1250.00,
    "currency": "SAR",
    "refundReason": "POLICY_CANCELLATION",
    "paymentMethod": "MADA",
    "gatewayReference": "RF-987654321",
    "processedAt": "2026-06-15T10:00:00.000Z"
  }
}
```

---

### 2.4 Customer Events

#### CustomerCreated (v1.0)

**Producer:** Customer Service  
**Consumers:** Audit  
**Trigger:** New customer registered

```json
{
  "eventType": "CustomerCreated",
  "eventVersion": "1.0",
  "sourceService": "customer-service",
  "data": {
    "customerId": "b2c3d4e5-...",
    "customerNumber": "CUST-2026-00001",
    "identityType": "CITIZEN",
    "fullNameAr": "أحمد بن محمد الحربي",
    "fullNameEn": "Ahmed Mohammed Al-Harbi",
    "nationalityCode": "SAU",
    "regionCode": "RIYADH",
    "createdAt": "2026-01-01T08:00:00.000Z"
  }
}
```

#### IdentityVerified (v1.0)

**Producer:** Policy Service  
**Consumers:** Audit  
**Trigger:** Yakeen identity verification completed

```json
{
  "eventType": "IdentityVerified",
  "eventVersion": "1.0",
  "sourceService": "policy-service",
  "data": {
    "customerId": "b2c3d4e5-...",
    "nationalId": "***masked***",
    "verificationStatus": "VERIFIED",
    "verificationReference": "YAK-2026-89123",
    "verifiedAt": "2026-01-01T08:05:00.000Z",
    "identityProvider": "YAKEEN"
  }
}
```

---

### 2.5 Integration Events

#### NajmDataRetrieved (v1.0)

**Producer:** Policy Service  
**Consumers:** Audit  
**Trigger:** Najm accident history data fetched

```json
{
  "eventType": "NajmDataRetrieved",
  "eventVersion": "1.0",
  "sourceService": "policy-service",
  "data": {
    "policyId": "a1b2c3d4-...",
    "requestType": "VEHICLE",
    "requestIdentifier": "1234567890",
    "claimsCount": 1,
    "riskScore": "LOW",
    "retrievedAt": "2026-01-01T08:10:00.000Z"
  }
}
```

#### SamaReportGenerated (v1.0)

**Producer:** Reporting Service  
**Consumers:** Audit  
**Trigger:** SAMA regulatory report generated

```json
{
  "eventType": "SamaReportGenerated",
  "eventVersion": "1.0",
  "sourceService": "reporting-service",
  "data": {
    "reportType": "MOTOR_POLICY_REGISTER",
    "reportPeriod": "2026-Q1",
    "recordCount": 1500,
    "totalPremium": 3750000.00,
    "currency": "SAR",
    "fileReference": "SAMA-2026-Q1-MOTOR-POLICY.xml",
    "generatedAt": "2026-04-01T02:00:00.000Z"
  }
}
```

---

## 3. Kafka Topic Configuration

### 3.1 Topic Definitions

```yaml
# kafka-topics.yaml
topics:
  - name: insurance.policy.events
    partitions: 6
    replicationFactor: 3
    retentionMs: 604800000    # 7 days
    cleanupPolicy: delete
    config:
      min.insync.replicas: 2

  - name: insurance.claims.events
    partitions: 6
    replicationFactor: 3
    retentionMs: 604800000
    cleanupPolicy: delete
    config:
      min.insync.replicas: 2

  - name: insurance.billing.events
    partitions: 3
    replicationFactor: 3
    retentionMs: 604800000
    cleanupPolicy: delete
    config:
      min.insync.replicas: 2

  - name: insurance.customer.events
    partitions: 3
    replicationFactor: 3
    retentionMs: 604800000
    cleanupPolicy: delete
    config:
      min.insync.replicas: 2

  - name: insurance.integration.events
    partitions: 3
    replicationFactor: 3
    retentionMs: 604800000
    cleanupPolicy: delete
    config:
      min.insync.replicas: 2

  - name: insurance.dead.letter.queue
    partitions: 1
    replicationFactor: 3
    retentionMs: 2592000000   # 30 days
    cleanupPolicy: delete
```

### 3.2 Producer Configuration

```yaml
# application.yml — Kafka producer
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      retries: 3
      properties:
        enable.idempotence: true
        max.in.flight.requests.per.connection: 5
        compression.type: snappy
        linger.ms: 5
        batch.size: 16384
```

### 3.3 Consumer Configuration

```yaml
# application.yml — Kafka consumer
spring:
  kafka:
    consumer:
      group-id: ${spring.application.name}
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
        spring.json.type.mapping: "event:com.enterprise.insurance.core.event.DomainEvent"
      auto-offset-reset: earliest
      enable-auto-commit: false
      max-poll-records: 50
    listener:
      ack-mode: manual
      concurrency: 3
```

---

## 4. Event Publishing Pattern

```java
// Event publisher interface
public interface EventPublisher {
    void publish(DomainEvent event);
}

// Implementation
@Component
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;
    private final TopicResolver topicResolver;

    @Override
    public void publish(DomainEvent event) {
        String topic = topicResolver.resolve(event.getEventType());
        String key = event.getSourceEntityId();
        
        kafkaTemplate.send(topic, key, event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event: {}", event.getEventId(), ex);
                    // Publish to DLQ
                    kafkaTemplate.send("insurance.dead.letter.queue", key, event);
                } else {
                    log.info("Event published: {} to topic: {} at offset: {}",
                        event.getEventType(), topic, result.getRecordMetadata().offset());
                }
            });
    }
}

// Topic resolution
@Component
public class TopicResolver {
    
    private static final Map<String, String> TOPIC_MAP = Map.ofEntries(
        Map.entry("PolicyIssued", "insurance.policy.events"),
        Map.entry("PolicyCancelled", "insurance.policy.events"),
        Map.entry("PolicyRenewed", "insurance.policy.events"),
        Map.entry("PolicyEndorsed", "insurance.policy.events"),
        Map.entry("ClaimRegistered", "insurance.claims.events"),
        Map.entry("ClaimUpdated", "insurance.claims.events"),
        Map.entry("ClaimClosed", "insurance.claims.events"),
        Map.entry("FraudAlertTriggered", "insurance.claims.events"),
        Map.entry("PaymentReceived", "insurance.billing.events"),
        Map.entry("PaymentFailed", "insurance.billing.events"),
        Map.entry("RefundProcessed", "insurance.billing.events"),
        Map.entry("CustomerCreated", "insurance.customer.events"),
        Map.entry("IdentityVerified", "insurance.customer.events"),
        Map.entry("NajmDataRetrieved", "insurance.integration.events"),
        Map.entry("SamaReportGenerated", "insurance.integration.events")
    );
    
    public String resolve(String eventType) {
        return TOPIC_MAP.getOrDefault(eventType, "insurance.dead.letter.queue");
    }
}
```

---

## 5. Event Consumer Example

```java
@Component
public class PolicyEventConsumer {

    @KafkaListener(topics = "insurance.policy.events", groupId = "notification-service")
    public void onPolicyIssued(PolicyIssuedEvent event, Acknowledgment ack) {
        log.info("Processing PolicyIssued event: {}", event.getEventId());
        
        // Send policy issuance SMS/email
        notificationService.sendPolicyIssuedNotification(
            event.getData().getCustomerId(),
            event.getData().getPolicyNumber()
        );
        
        ack.acknowledge();
    }

    @KafkaListener(topics = "insurance.billing.events", groupId = "policy-service")
    public void onPaymentReceived(PaymentReceivedEvent event, Acknowledgment ack) {
        log.info("Processing PaymentReceived event: {}", event.getEventId());
        
        // Activate policy after payment
        policyService.activatePolicy(event.getData().getPolicyId());
        
        ack.acknowledge();
    }
}
```

---

## 6. Dead Letter Queue Handling

Events that fail to process after retries are routed to the dead letter queue:

```java
@Component
public class DeadLetterQueueProcessor {

    @KafkaListener(topics = "insurance.dead.letter.queue", groupId = "dlq-processor")
    public void processDeadLetter(DomainEvent event, Acknowledgment ack) {
        log.error("Processing dead letter event: {} type: {}",
            event.getEventId(), event.getEventType());
        
        // Store in database for manual review
        deadLetterRepository.save(DeadLetterEntry.builder()
            .eventId(event.getEventId())
            .eventType(event.getEventType())
            .payload(event)
            .failedAt(Instant.now())
            .status("PENDING_REVIEW")
            .build());
        
        // Alert operations team
        alertService.sendAlert("DLQ_EVENT", 
            "Event " + event.getEventId() + " failed processing");
        
        ack.acknowledge();
    }
}
```

---

## 7. Event Versioning Strategy

| Version Change | Action | Backward Compatible |
|----------------|--------|---------------------|
| Add optional field | New schema version | Yes |
| Add required field | New schema version + consumer update | No |
| Rename field | New schema version + consumer update | No |
| Remove field | New schema version + consumer update | No |
| Change field type | New schema version + consumer update | No |

**Rules:**
- Producers must not remove fields from existing event versions
- New fields must be optional (nullable) for at least one minor version
- Consumers must tolerate unknown fields
- Event schema versions follow semantic versioning (MAJOR.MINOR)

---

## Document Maintenance

| Aspect | Detail |
|--------|--------|
| Last Updated | 2026-07-06 |
| Owner | Enterprise Architecture Team |
| Review Cycle | Quarterly, or when new events are added |
| Related Documents | [Integration Architecture](integration-architecture.md), [Data Architecture](data-architecture.md) |
