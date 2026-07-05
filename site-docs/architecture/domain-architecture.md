# Domain Architecture

## Motor-First, Extensible-by-Design Architecture

This document defines the architecture for an MVP that ships with Motor Insurance first for the Saudi Arabia market, while keeping the platform extensible for Health, Travel, and Malpractice in a later phase. The design is intentionally metadata-driven so business users and administrators can add new fields, entities, and rules from the UI with minimal engineering intervention.

---

## Section 1: The Extensible Core (Shared Kernel)

The shared kernel contains abstractions that are owned by the platform and must not become line-specific. These types define the contract that every line must honor.

### 1.1 Core Base Entities

- `Party` — base person or organization entity
- `Customer` — concrete extension of `Party` that owns the global identity
- `Policy` — abstract base policy class
- `Claim` — abstract base claim class
- `FinancialTransaction` — abstract financial event type

### 1.2 Base Customer Model

```java
public abstract class Party {
    private UUID partyId;
    private String fullName;
    private Address primaryAddress;
    private ContactInfo contactInfo;

    public UUID getPartyId() { return partyId; }
    public void setPartyId(UUID partyId) { this.partyId = partyId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Address getPrimaryAddress() { return primaryAddress; }
    public void setPrimaryAddress(Address primaryAddress) { this.primaryAddress = primaryAddress; }
    public ContactInfo getContactInfo() { return contactInfo; }
    public void setContactInfo(ContactInfo contactInfo) { this.contactInfo = contactInfo; }
}

public class Customer extends Party {
    private UUID globalCustomerId;
    private String customerReference;
    private String customerType;

    public UUID getGlobalCustomerId() { return globalCustomerId; }
    public void setGlobalCustomerId(UUID globalCustomerId) { this.globalCustomerId = globalCustomerId; }
    public String getCustomerReference() { return customerReference; }
    public void setCustomerReference(String customerReference) { this.customerReference = customerReference; }
    public String getCustomerType() { return customerType; }
    public void setCustomerType(String customerType) { this.customerType = customerType; }
}
```

### 1.3 Base Policy Model

```java
public enum PolicyLifecycleStatus {
    QUOTE,
    BOUND,
    ACTIVE,
    RENEWED,
    CANCELLED,
    EXPIRED
}

public abstract class Policy {
    private UUID policyId;
    private UUID globalCustomerId;
    private LineOfBusiness lineOfBusiness;
    private PolicyLifecycleStatus status;
    private BigDecimal premiumAmount;
    private Currency currency;
    private Instant effectiveFrom;
    private Instant effectiveTo;
    private JsonNode lineSpecificData;

    // getters and setters
}
```

### 1.4 Base Claim Model

```java
public enum ClaimLifecycleStatus {
    REPORTED,
    INVESTIGATING,
    RESERVED,
    ADJUDICATED,
    PAID,
    CLOSED
}

public abstract class Claim {
    private UUID claimId;
    private UUID policyId;
    private ClaimLifecycleStatus status;
    private BigDecimal claimAmount;
    private String claimReference;
    private JsonNode lineSpecificData;

    // getters and setters
}
```

### 1.5 Base Financial Transaction Types

```java
public enum FinancialTransactionType {
    PREMIUM,
    ENDORSEMENT,
    REFUND,
    COMMISSION,
    RECOVERY
}
```

### 1.6 Dynamic Extension Model

The platform uses a metadata-driven extension layer that allows the UI and runtime to introduce new schema elements without hard-coding them in the core domain model.

#### Core Concepts

- `EntityDefinition` — defines a logical entity such as `MotorPolicy`, `Claim`, or a future `HealthMember`
- `FieldDefinition` — defines a field with type, validation, UI rendering, and default behavior
- `FormDefinition` — defines which fields appear on a screen and in what order
- `RuleDefinition` — defines validation, calculation, or workflow triggers

```java
public class EntityDefinition {
    private String entityCode;
    private String displayName;
    private String tableName;
    private List<FieldDefinition> fields;
}

public class FieldDefinition {
    private String fieldCode;
    private String displayName;
    private FieldType fieldType;
    private boolean required;
    private boolean searchable;
    private String defaultValue;
}
```

#### Runtime Behavior

- UI forms are generated from `FormDefinition` metadata
- Data is stored in a flexible attribute store for dynamic fields
- Validation and calculation rules are executed from `RuleDefinition`
- New extensions are versioned so they can be safely rolled out and reverted

### 1.7 Shared Kernel Ownership Rules

The shared kernel must remain stable and should not contain:

- Motor-only fields such as VIN
- Health-only fields such as ICD codes
- Travel-only fields such as trip destination
- Malpractice-only fields such as specialty code

This is the invariant: the shared kernel defines lifecycle, identity, and financial primitives only.

---

## Section 2: The Motor Vertical Slice

Motor is the only line implemented today. All Motor-specific logic sits under the Motor vertical slice so the team can move quickly without contaminating the core.

### 2.1 Motor Policy Model

```java
public class MotorPolicy extends Policy {
    private String vin;
    private String vehicleMake;
    private String vehicleModel;
    private Integer vehicleYear;
    private Address garagingAddress;
    private Integer annualMileage;
    private VehicleUseType primaryUse;
    private boolean antiTheftDeviceInstalled;

    // getters and setters
}

public enum VehicleUseType {
    PERSONAL,
    COMMERCIAL
}
```

### 2.2 Motor Driver Entity

```java
public class MotorDriver {
    private UUID driverId;
    private UUID policyId;
    private String licenseNumber;
    private LocalDate dateOfBirth;
    private List<String> drivingViolations;
    private List<String> claimsHistory;

    // getters and setters
}
```

### 2.3 Motor Vehicle Entity

```java
public class MotorVehicle {
    private UUID vehicleId;
    private UUID policyId;
    private String vin;
    private String vehicleMake;
    private String vehicleModel;
    private Integer vehicleYear;
    private Address garagingAddress;

    // getters and setters
}
```

### 2.4 Motor Rating Engine

The Motor rating engine must live in the Motor vertical slice and use line-specific factors only.

#### Rating Factors

- Territory factor
- Driver age multiplier
- Vehicle value depreciation curve
- Annual mileage factor
- Use type factor (Commercial vs Personal)
- Anti-theft device discount
- Claims history surcharge
- Violations surcharge

```java
public class MotorRatingEngine {
    public BigDecimal calculatePremium(MotorPolicy policy, List<MotorDriver> drivers) {
        BigDecimal baseRate = new BigDecimal("1000");
        BigDecimal territoryFactor = getTerritoryFactor(policy.getGaragingAddress());
        BigDecimal ageMultiplier = getAgeMultiplier(drivers);
        BigDecimal valueDepreciationFactor = getDepreciationFactor(policy.getVehicleYear());
        BigDecimal mileageFactor = getMileageFactor(policy.getAnnualMileage());
        BigDecimal useFactor = policy.getPrimaryUse() == VehicleUseType.COMMERCIAL ? new BigDecimal("1.25") : BigDecimal.ONE;
        BigDecimal theftDiscount = policy.isAntiTheftDeviceInstalled() ? new BigDecimal("0.90") : BigDecimal.ONE;

        return baseRate.multiply(territoryFactor)
                .multiply(ageMultiplier)
                .multiply(valueDepreciationFactor)
                .multiply(mileageFactor)
                .multiply(useFactor)
                .multiply(theftDiscount);
    }
}
```

### 2.5 Motor Claims Workflow

Motor claims need specialized handling that should not leak into the core.

#### Motor Claim Types

- Auto Physical Damage (APD)
- Third-Party Liability
- Theft claim
- Towing assistance

#### Motor-Specific Operations

- APD appraisal
- Tow truck dispatch
- Repair shop network management
- Salvage valuation

```java
public class MotorClaim extends Claim {
    private String lossLocation;
    private String repairShopId;
    private boolean towTruckRequested;
    private Instant towTruckDispatchedAt;
    private String appraisalReference;

    // getters and setters
}
```

---

## Section 3: The Pluggable Extension Points

These are the explicit seams that allow future lines to plug in without rewriting the platform.

### Extension Point A: Dynamic Data Schema

The platform should support dynamic data extension through a metadata layer and a flexible persistence structure.

#### Recommended Approach

Use a hybrid strategy:
- Core business entities remain strongly typed in Java classes
- Dynamic fields are stored in `JSONB` and resolved by the metadata layer
- UI forms are rendered from `FieldDefinition` and `FormDefinition`

This enables the system to stay robust for the core while remaining flexible for business-driven changes.

Recommendation: use PostgreSQL `JSONB` in the shared policy and customer records for extensible attributes.

#### Why `JSONB` instead of EAV?

- `JSONB` supports nested structures naturally for future lines
- It is easier to query than a generic EAV model
- It preserves a strong, schema-aware shape for the current Motor implementation
- It avoids the performance and join complexity of EAV

#### Data Model Pattern

```sql
CREATE TABLE customer (
  customer_id UUID PRIMARY KEY,
  global_customer_id UUID NOT NULL,
  customer_type TEXT NOT NULL,
  core_profile JSONB NOT NULL,
  line_specific_data JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE policy (
  policy_id UUID PRIMARY KEY,
  global_customer_id UUID NOT NULL,
  line_of_business TEXT NOT NULL,
  status TEXT NOT NULL,
  line_specific_data JSONB NOT NULL DEFAULT '{}'::jsonb
);
```

#### Example Motor Payload

```json
{
  "vehicle": {
    "vin": "1HGCM82633A004352",
    "make": "Toyota",
    "model": "Corolla",
    "year": 2021,
    "annualMileage": 12000,
    "primaryUse": "Personal",
    "antiTheftDeviceInstalled": true
  },
  "drivers": [
    {
      "licenseNumber": "ABC123",
      "dateOfBirth": "1990-05-15",
      "violations": [],
      "claimsHistory": []
    }
  ]
}
```

Future lines can use the same column for health or travel-specific payloads.

### Extension Point B: Pluggable Rating Engine

The rating engine is a runtime extension point.

```java
public interface IRatingCalculator {
    LineOfBusiness getLineOfBusiness();
    BigDecimal calculate(Policy policy);
}

public class MotorRatingCalculator implements IRatingCalculator {
    @Override
    public LineOfBusiness getLineOfBusiness() {
        return LineOfBusiness.MOTOR;
    }

    @Override
    public BigDecimal calculate(Policy policy) {
        MotorPolicy motorPolicy = (MotorPolicy) policy;
        return new MotorRatingEngine().calculatePremium(motorPolicy, List.of());
    }
}
```

Runtime resolution:

```java
public class RatingService {
    private final List<IRatingCalculator> calculators;

    public RatingService(List<IRatingCalculator> calculators) {
        this.calculators = calculators;
    }

    public BigDecimal calculate(Policy policy) {
        return calculators.stream()
                .filter(c -> c.getLineOfBusiness() == policy.getLineOfBusiness())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No calculator registered"))
                .calculate(policy);
    }
}
```

#### DI Registration

```java
@Bean
public IRatingCalculator motorRatingCalculator() {
    return new MotorRatingCalculator();
}
```

When Health is onboarded, a new implementation such as `HealthRatingCalculator` can be added without changing the service interface.

### Extension Point C: Configurable Workflow Engine

Motor claims need custom states and transitions, and future lines will need their own state machines.

#### Generic Workflow Model

```java
public class WorkflowDefinition {
    private String workflowKey;
    private List<WorkflowState> states;
    private List<WorkflowTransition> transitions;
}

public class WorkflowState {
    private String stateCode;
    private String displayName;
}

public class WorkflowTransition {
    private String fromState;
    private String toState;
    private String trigger;
}
```

#### Motor Example

- `REPORTED -> INSPECTION_REQUIRED`
- `INSPECTION_REQUIRED -> TOW_DISPATCHED`
- `TOW_DISPATCHED -> REPAIR_SCHEDULED`
- `REPAIR_SCHEDULED -> ADJUDICATED`

#### Health Example Later

- `REPORTED -> PREAUTH_REQUIRED`
- `PREAUTH_REQUIRED -> APPROVED`
- `APPROVED -> CLAIM_PAID`

The workflow engine itself stays generic. Each line registers its states and transitions from configuration or a database-backed rules table.

### Extension Point D: Line-Specific Underwriting Rules

Underwriting rules are injected at runtime by line.

```java
public interface IUnderwritingRule {
    LineOfBusiness getLineOfBusiness();
    boolean evaluate(Policy policy, UnderwritingContext context);
    String getRuleCode();
}
```

#### Motor Example

```java
public class DriverAgeRule implements IUnderwritingRule {
    @Override
    public LineOfBusiness getLineOfBusiness() {
        return LineOfBusiness.MOTOR;
    }

    @Override
    public String getRuleCode() {
        return "MOTOR_DRIVER_AGE_REFERRAL";
    }

    @Override
    public boolean evaluate(Policy policy, UnderwritingContext context) {
        return context.getDriverAge() < 21;
    }
}
```

#### Runtime Loading

```java
public class UnderwritingService {
    private final List<IUnderwritingRule> rules;

    public UnderwritingService(List<IUnderwritingRule> rules) {
        this.rules = rules;
    }

    public List<String> evaluate(Policy policy) {
        return rules.stream()
                .filter(rule -> rule.getLineOfBusiness() == policy.getLineOfBusiness())
                .filter(rule -> rule.evaluate(policy, new UnderwritingContext()))
                .map(IUnderwritingRule::getRuleCode)
                .toList();
    }
}
```

Rules can be loaded from a database table such as `underwriting_rule_definition` or from an external rule repository service.

---

## Section 4: Repository and Team Structure

This repository structure is designed to keep Motor output fast today while keeping future lines isolated.

### 4.1 Folder Structure

```text
/src
  /Core
    /Domain
      Policy.cs
      Claim.cs
      Party.cs
      FinancialTransaction.cs
    /Contracts
      IRatingCalculator.cs
      IUnderwritingRule.cs
    /SharedKernel
      enums/
      value-objects/
  /Lines
    /Motor
      /Api
      /Application
      /Domain
      /Infrastructure
      /Db
        /Migrations
      /README.md
    /Health
      /README.md
  /SharedServices
    /Billing
    /GL
    /Reinsurance
    /Documents
```

### 4.2 Ownership Rules

- `Core` owns the abstract models and platform contracts
- `Lines/Motor` owns all Motor-specific code, APIs, database migrations, and workflow definitions
- `Lines/Health` is empty today and acts as a future onboarding skeleton
- `SharedServices` owns line-agnostic services used by all lines

### 4.3 Database Migration Strategy

Motor must not force migrations against a central shared schema.

#### Recommended Pattern

- Shared kernel tables live in a shared schema or shared database
- Motor-specific tables live in a `motor` schema or a `motor` database
- Motor migrations run only against the Motor schema
- Shared services remain independent and own their own migration sets

#### Example

```sql
-- Shared schema
CREATE TABLE policy (...);
CREATE TABLE claim (...);
CREATE TABLE customer (...);

-- Motor schema
CREATE TABLE motor_policy_extension (...);
CREATE TABLE motor_driver (...);
CREATE TABLE motor_vehicle (...);
CREATE TABLE motor_claim_detail (...);
```

This means Team Motor can add a column to `motor_policy_extension` without changing the shared kernel schema.

---

## Section 5: The Onboarding Playbook for a New Line

### 30-Day Onboarding Path for Team Health

1. Create a new folder under `/src/Lines/Health`.
2. Implement `IRatingCalculator` with a `HealthRatingCalculator`.
3. Register workflow states and transitions in the workflow engine configuration.
4. Extend the base customer or policy via the `line_specific_data` JSONB column.
5. Deploy an independently scalable microservice that registers itself with the API Gateway.

### Detailed Execution Notes

- Use the existing shared kernel interfaces and avoid creating line-specific overrides in the core
- Keep the new line's domain logic isolated under its own folder
- Use the same API contract patterns as Motor for discoverability and future interoperability
- Ensure the line service emits events compatible with the platform event bus

---

## Extensibility Scorecard

| Module | Motor Implementation Today | Extensibility Mechanism |
|---|---|---|
| Policy Data | `MotorPolicy : Policy` with VIN, vehicle attributes, garaging address, annual mileage, use type, anti-theft flag | `Policy.LineSpecificData` JSONB plus line-specific tables in the Motor schema |
| Rating | `MotorRatingEngine` and `MotorRatingCalculator` | `IRatingCalculator` with DI-based registration per `LineOfBusiness` |
| Underwriting | Rules such as driver age referral and violation-based review | `IUnderwritingRule` with runtime-loaded rules from DB or external repository |
| Claims | `MotorClaim` with APD appraisal, tow truck dispatch, repair shop handling | Configurable workflow engine with line-specific states and transitions |
| Billing | Line-agnostic financial transactions using `FinancialTransactionType` | Shared services in `/src/SharedServices` plus line-specific extension data for billing context |
